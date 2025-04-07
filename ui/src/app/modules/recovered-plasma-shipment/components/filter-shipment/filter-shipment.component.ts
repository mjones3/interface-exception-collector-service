import { CommonModule, formatDate } from '@angular/common';
import {
    Component,
    computed,
    inject,
    input,
    LOCALE_ID,
    OnInit,
    output,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { DateRangePickerComponent } from 'app/shared/components/date-range-picker/date-range-picker.component';
import { FiltersComponent } from 'app/shared/components/filters/filters.component';
import { MultipleSelectComponent } from 'app/shared/components/multiple-select/multiple-select.component';
import { RecoveredPlasmaLocationDTO } from '../../graphql/query-definitions/location.graphql';
import { RecoveredPlasmaCustomerDTO } from '../../graphql/query-definitions/customer.graphql';
import { LookUpDto, SelectOptionDto } from '@shared';
import { RecoveredPlasmaShipmentQueryCommandRequestDTO } from '../../graphql/query-definitions/shipment.graphql';
import { SELECT_ALL_VALUE } from '../../../../shared/utils/mat-select-trigger.utils';

interface FilterShipmentFormDateRange {
    start: Date;
    end: Date;
}

interface FilterShipmentForm {
    locationCode: string[];
    shipmentNumber: string;
    shipmentStatus: string[];
    customers: string[];
    productTypes: string[];
    shipmentDate: FilterShipmentFormDateRange;
}

@Component({
    selector: 'biopro-filter-shipment',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        FormsModule,
        CommonModule,
        MatDividerModule,
        MatIconModule,
        FiltersComponent,
        MatSelectModule,
        MatButtonModule,
        MultipleSelectComponent,
        MatDatepickerModule,
        DateRangePickerComponent,
    ],
    templateUrl: './filter-shipment.component.html',
})
export class FilterShipmentComponent implements OnInit {
    locale: string = inject(LOCALE_ID);
    formBuilder = inject(FormBuilder);
    form: FormGroup = this.formBuilder.group({
        locationCode: [[]],
        shipmentNumber: [null, [Validators.maxLength(50)]],
        shipmentStatus: [[]],
        customers: [[]],
        productTypes: [[]],
        shipmentDate: this.formBuilder.group({
            start: [], // Start date
            end: [], // End date
        }),
        transportationReferenceNumber: [],
    });

    showFilters = input(false);
    locations = input<RecoveredPlasmaLocationDTO[]>([]);
    customers = input<RecoveredPlasmaCustomerDTO[]>([]);
    productTypes = input<LookUpDto[]>([]);
    shipmentTypes = input<LookUpDto[]>([]);

    locationsOptions = computed<SelectOptionDto[]>(() =>
        this.locations().map((item) => ({
            optionKey: item.code,
            optionDescription: item.name,
        }))
    );
    customersOptions = computed<SelectOptionDto[]>(() =>
        this.customers().map((item) => ({
            optionKey: item.code,
            optionDescription: item.name,
        }))
    );
    productTypesOptions = computed<SelectOptionDto[]>(() =>
        this.productTypes().map((item) => ({
            optionKey: item.optionValue,
            optionDescription: item.descriptionKey,
        }))
    );
    shipmentTypesOptions = computed<SelectOptionDto[]>(() =>
        this.shipmentTypes().map((item) => ({
            optionKey: item.optionValue,
            optionDescription: item.descriptionKey,
        }))
    );

    onApplySearchFilters = output<RecoveredPlasmaShipmentQueryCommandRequestDTO>();
    onResetFilters = output<RecoveredPlasmaShipmentQueryCommandRequestDTO>();
    toggleFilters = output<boolean>();
    enableApplyFilters = signal(false);
    totalFieldsApplied = signal(0);
    today = new Date();

    ngOnInit() {
        this.monitorChanges();
    }

    private monitorChanges() {
        const { locationCode, shipmentNumber, shipmentStatus, customers, productTypes, shipmentDate, transportationReferenceNumber } = this.form.controls;
        const criteriaSingle = shipmentNumber;
        const criteriaMultiple = [ locationCode, shipmentStatus, customers, productTypes, shipmentDate, transportationReferenceNumber ];
        const startShipmentDate = shipmentDate.get('start');
        const endShipmentDate = shipmentDate.get('end');

        this.form.valueChanges.subscribe((value: FilterShipmentForm) => {
            if (value.shipmentNumber) {
                criteriaSingle.enable({ emitEvent : false });
                criteriaMultiple
                    .filter(control => !control.disabled)
                    .forEach(control => control.disable({ emitEvent : false }));
                startShipmentDate.removeValidators([ Validators.required ]);
                endShipmentDate.removeValidators([ Validators.required ]);
            } else if (value.locationCode?.length || value.shipmentStatus?.length || value.customers?.length || value.productTypes?.length || value.shipmentDate?.start || value.shipmentDate?.end) {
                criteriaSingle.disable({ emitEvent : false });
                criteriaMultiple
                    .filter(control => control.disabled)
                    .forEach(control => control.enable({ emitEvent : false }));
                if (!startShipmentDate.hasValidator(Validators.required)) {
                    startShipmentDate.addValidators([ Validators.required ]);
                    startShipmentDate.updateValueAndValidity({ emitEvent : false });
                    startShipmentDate.markAsTouched({ emitEvent : false });
                }
                if (!endShipmentDate.hasValidator(Validators.required)) {
                    endShipmentDate.addValidators([ Validators.required ]);
                    endShipmentDate.updateValueAndValidity({ emitEvent : false });
                    endShipmentDate.markAsTouched({ emitEvent : false });
                }
            } else {
                this.form.enable({ emitEvent : false });
            }
            this.enableApplyFilters.set(this.form.errors == null && this.form.valid);
        });
    }

    totalFieldsInformed(): number {
        return Object.keys(this.form.value).filter((key) =>
            this.isFieldInformed(key)
        ).length;
    }

    emitResults(criteria: RecoveredPlasmaShipmentQueryCommandRequestDTO) {
        this.totalFieldsApplied.set(this.totalFieldsInformed());
        this.onApplySearchFilters.emit(criteria);
    }

    dropSelectAllOptionFrom(array: string[]): string[] {
        return array?.filter((i: string) => i !== SELECT_ALL_VALUE);
    }

    formatToISO(date: string | number | Date): string {
        return formatDate(date, 'yyyy-MM-dd', this.locale, 'UTC');
    }

    applyFilterSearch(): void {
        const formValue = this.form.value;
        const criteria: RecoveredPlasmaShipmentQueryCommandRequestDTO = {
            ...(formValue.locationCode?.length
                ? {
                      locationCode: this.dropSelectAllOptionFrom(
                          formValue.locationCode
                      ),
                  }
                : null),
            ...(formValue.shipmentNumber
                ? { shipmentNumber: formValue.shipmentNumber }
                : null),
            ...(formValue.shipmentStatus?.length
                ? {
                      shipmentStatus: this.dropSelectAllOptionFrom(
                          formValue.shipmentStatus
                      ),
                  }
                : null),
            ...(formValue.customers?.length
                ? {
                      customers: this.dropSelectAllOptionFrom(
                          formValue.customers
                      ),
                  }
                : null),
            ...(formValue.productTypes?.length
                ? {
                      productTypes: this.dropSelectAllOptionFrom(
                          formValue.productTypes
                      ),
                  }
                : null),
            ...(formValue.shipmentDate
                ? {
                      shipmentDateFrom: this.formatToISO(
                          formValue.shipmentDate?.start
                      ),
                      shipmentDateTo: this.formatToISO(
                          formValue.shipmentDate?.end
                      ),
                  }
                : null),
            ...(formValue.transportationReferenceNumber
                ? { transportationReferenceNumber: formValue.transportationReferenceNumber }
                : null)
        };
        this.emitResults(criteria);
    }

    resetFilters(): void {
        this.form.reset();
        this.form.enable({ emitEvent: false })
        this.emitNoResults();
    }

    emitNoResults() {
        this.totalFieldsApplied.set(this.totalFieldsInformed());
        this.onResetFilters.emit(null);
    }

    toggleFilter(toggleFlag: boolean): void {
        this.toggleFilters.emit(toggleFlag);
    }

    getDateRangeFormGroup(formGroup: string): FormGroup {
        const control = this.form.get(formGroup);
        return control instanceof FormGroup ? control : null;
    }

    isFieldInformed(key: string) {
        if (key === 'shipmentDate') {
            return (
                this.isDateRangeInformed(this.form.value[key]) ||
                this.isInvalidDateRangeInformed(key)
            );
        } else if (Array.isArray(this.form.value[key])) {
            return this.form.value[key].length > 0;
        } else {
            return this.form.value[key] != null && this.form.value[key] !== '';
        }
    }

    isDateRangeInformed(date: { start: Date; end: Date }): boolean {
        return date != null && (date?.start != null || date?.end != null);
    }

    isInvalidDateRangeInformed(field: 'shipmentDate'): boolean {
        const startDate = this.form.get(field)?.get('start');
        const endDate = this.form.get(field)?.get('end');
        return startDate?.hasError('matStartDateInvalid')
            || startDate?.hasError('matDatepickerParse')
            || endDate?.hasError('matDatepickerParse')
            || endDate?.hasError('matEndDateInvalid');
    }
}
