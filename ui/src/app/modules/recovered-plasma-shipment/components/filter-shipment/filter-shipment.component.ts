import { CommonModule } from '@angular/common';
import { Component, computed, input, OnInit, output } from '@angular/core';
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

    toggleFilters = output<boolean>();
    totalFieldsApplied = 0;
    today = new Date();
    form: FormGroup;

    constructor(private formBuilder: FormBuilder) {}

    ngOnInit(): void {
        this.initForm();
    }

    private initForm() {
        this.form = this.formBuilder.group({
            shipmentNumber: ['', [Validators.maxLength(50)]],
            shipmentStatus: [''],
            productFamily: [''],
            customer: [''],
            shipmentDate: this.formBuilder.group({
                start: [null], // Start date
                end: [null], // End date
            }),
            location: [''],
            transportationReferenceNumber: [''],
        });
    }

    resetFilters(): void {
        this.form.reset();
    }

    toggleFilter(toggleFlag: boolean): void {
        this.toggleFilters.emit(toggleFlag);
    }

    getDateRangeFormGroup(formGroup: string): FormGroup {
        const control = this.form.get(formGroup);
        return control instanceof FormGroup ? control : null;
    }
}
