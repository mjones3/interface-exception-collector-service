import {
    animate,
    state,
    style,
    transition,
    trigger,
} from '@angular/animations';
import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    OnInit,
    input,
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
import { ApolloError } from '@apollo/client';
import { AutoUnsubscribe, SelectOptionDto } from '@shared';
import { ToastrService } from 'ngx-toastr';
import { DateRangePickerComponent } from '../../../../../shared/components/date-range-picker/date-range-picker.component';
import { FiltersComponent } from '../../../../../shared/components/filters/filters.component';
import { MultipleSelectComponent } from '../../../../../shared/components/multiple-select/multiple-select.component';
import { BioproValidators } from '../../../../../shared/forms/biopro-validators';
import { OrderPriorityMap } from '../../../../../shared/models/order-priority.model';
import { OrderStatusMap } from '../../../../../shared/models/order-status.model';
import { SearchOrderFilterDTO } from '../../../models/order.dto';
import { OrderService } from '../../../services/order.service';
import { SearchSelectComponent } from 'app/shared/components/search-select/search-select.component';
import { ShipmentTypeMap } from '../../../../../shared/models/shipment-type.model';
import { OptionDTO } from '../../../../../shared/models/option.dto';

@Component({
    selector: 'app-search-order-filter',
    animations: [
        trigger('detailExpand', [
            state('collapsed,void', style({ height: '0px', minHeight: '0' })),
            state('expanded', style({ height: '*' })),
            transition(
                'expanded <=> collapsed',
                animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')
            ),
        ]),
    ],
    standalone: true,
    changeDetection: ChangeDetectionStrategy.OnPush,
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        FormsModule,
        CommonModule,
        MatDividerModule,
        SearchSelectComponent,
        MatIconModule,
        FiltersComponent,
        MatSelectModule,
        MatButtonModule,
        MultipleSelectComponent,
        MatDatepickerModule,
        DateRangePickerComponent,
    ],
    templateUrl: './search-order-filter.component.html',
    styleUrl: './search-order-filter.component.scss',
})
@AutoUnsubscribe()
export class SearchOrderFilterComponent implements OnInit {
    readonly OrderStatusMap = OrderStatusMap;
    readonly OrderPriorityMap = OrderPriorityMap;

    showFilters = input(false);
    onApplySearchFilters = output<SearchOrderFilterDTO>();
    onResetFilters = output<SearchOrderFilterDTO>();
    toggleFilters = output<boolean>();
    shipmentTypeSelected = signal<string>('');

    searchForm: FormGroup;

    statusOptions: SelectOptionDto[];
    priorityOptions: SelectOptionDto[];
    customers: SelectOptionDto[];
    locations: SelectOptionDto[];
    shipmentTypeOptions:OptionDTO[];

    totalFieldsApplied = 0;

    today = new Date();
    minCreateDateCriteria = new Date();

    constructor(
        private formBuilder: FormBuilder,
        private orderService: OrderService,
        private toaster: ToastrService
    ) {}

    ngOnInit(): void {
        this.loadCriteriaOptions();
        this.initForm();
        this.today.setHours(0, 0, 0, 0);
        this.monitorChanges();
    }

    private monitorChanges() {
        this.searchForm.valueChanges.subscribe((value) => {
            if (this.remainingFieldsInformed()) {
                this.searchForm
                    .get('orderNumber')
                    ?.disable({ emitEvent: false });
                Object.keys(this.searchForm.controls).forEach((key) => {
                    if (key !== 'orderNumber') {
                        this.searchForm.get(key)?.enable({ emitEvent: false });
                        if(this.shipmentTypeSelected() === '') {
                            this.searchForm.get('customers')?.disable({ emitEvent: false });
                            this.searchForm.get('locations')?.disable({ emitEvent: false });
                        } else if(this.shipmentTypeSelected() === 'INTERNAL_TRANSFER'){
                            this.searchForm.get('customers')?.disable({ emitEvent: false });
                        }else{
                            this.searchForm.get('locations')?.disable({ emitEvent: false });
                        }
                        if (key === 'createDate') {
                            const startControl = this.searchForm
                                .get(key)
                                ?.get('start');
                            startControl?.addValidators(Validators.required);
                            startControl?.updateValueAndValidity({
                                emitEvent: false,
                            });
                        }
                    }
                });
            } else if (this.orderNumberInformed()) {
                this.searchForm
                    .get('orderNumber')
                    ?.enable({ emitEvent: false });
                Object.keys(this.searchForm.controls).forEach((key) => {
                    if (key !== 'orderNumber') {
                        this.searchForm.get(key)?.disable({ emitEvent: false });
                        if (key === 'createDate') {
                            const startControl = this.searchForm
                                .get(key)
                                ?.get('start');
                            startControl?.removeValidators(Validators.required);
                            startControl?.updateValueAndValidity({
                                emitEvent: false,
                            });
                        }
                    }
                });
            } else {
                Object.keys(this.searchForm.controls).forEach((key) => {
                    this.searchForm.get(key)?.enable({ emitEvent: false });
                    if (key === 'orderNumber') {
                        this.searchForm
                            .get(key)
                            ?.removeValidators(Validators.required);
                    } else if (key === 'createDate') {
                        const startControl = this.searchForm
                            .get(key)
                            ?.get('start');
                        startControl?.removeValidators(Validators.required);
                        startControl?.updateValueAndValidity({
                            emitEvent: false,
                        });
                    }
                });
            }
        });
        //TODO
        this.searchForm.get('shipmentType').valueChanges.subscribe((value) => {
            this.shipmentTypeSelected.set(value);
            this.searchForm.get('locations').reset();
            this.searchForm.get('customers').reset();
        })
    }

    orderNumberInformed = () => this.isFieldInformed('orderNumber');

    remainingFieldsInformed() {
        const { orderNumber, ...otherFields } = this.searchForm.value;

        return Object.keys(otherFields).some((key) =>
            this.isFieldInformed(key)
        );
    }

    isInvalidDateRangeInformed = (field: 'createDate' | 'desiredShipDate') =>
        this.searchForm
            .get(field)
            ?.get('start')
            ?.hasError('matDatepickerParse') ||
        this.searchForm.get(field)?.get('end')?.hasError('matDatepickerParse');

    isDateRangeInformed = (date) =>
        date != null && (date?.start != null || date?.end != null);

    totalFieldsInformed = () =>
        Object.keys(this.searchForm.value).filter((key) =>
            this.isFieldInformed(key)
        ).length;

    isFieldInformed(key: string) {
        if (key === 'createDate' || key === 'desiredShipDate') {
            return (
                this.isDateRangeInformed(this.searchForm.value[key]) ||
                this.isInvalidDateRangeInformed(key)
            );
        } else if (Array.isArray(this.searchForm.value[key])) {
            return this.searchForm.value[key].length > 0;
        } else {
            return (
                this.searchForm.value[key] != null &&
                this.searchForm.value[key] !== ''
            );
        }
    }

    private loadCriteriaOptions() {
        this.orderService.searchOrderCriteria().subscribe({
            next: (response) => {
                this.statusOptions =
                    response.data.searchOrderCriteria.orderStatus.map(
                        (item) => ({
                            optionKey: item.optionValue,
                            optionDescription: OrderStatusMap[item.optionValue],
                        })
                    );
                this.priorityOptions =
                    response.data.searchOrderCriteria.orderPriorities.map(
                        (item) => ({
                            optionKey: item.optionValue,
                            optionDescription:
                                OrderPriorityMap[
                                    item.optionValue
                                ]?.toUpperCase(),
                        })
                    );
                this.shipmentTypeOptions =
                    response.data.searchOrderCriteria.shipmentTypes.map(
                        (item) => ({
                            code: item.optionValue,
                            name:ShipmentTypeMap[
                                item.optionValue
                                ]?.toUpperCase(),
                        })
                    );
                this.customers =
                    response.data.searchOrderCriteria.customers.map((item) => ({
                        optionKey: item.code,
                        optionDescription: item.name,
                    }));
                this.locations =
                    response.data.searchOrderCriteria.locations.map((item) => ({
                        optionKey: item.code,
                        optionDescription: item.name,
                    }));
            },
            error: (error: ApolloError) => {
                this.toaster.error('Something went wrong.');
            },
        });
    }

    private initForm() {
        this.minCreateDateCriteria = new Date();
        this.minCreateDateCriteria.setHours(0, 0, 0, 0);
        this.minCreateDateCriteria.setFullYear(
            this.minCreateDateCriteria.getFullYear() - 2
        );
        this.searchForm = this.formBuilder.group(
            {
                orderNumber: ['', [Validators.maxLength(50)]],
                orderStatus: [''],
                deliveryTypes: [''],
                shipmentType: [''],
                locations: [{value: '', disabled: false}],
                customers: [{value: '', disabled: false}],
                createDate: this.formBuilder.group({
                    start: [null], // Start date
                    end: [null], // End date
                }),
                desiredShipDate: this.formBuilder.group({
                    start: [null], // Start date
                    end: [null], // End date
                }),
            },
            {
                validators: [
                    BioproValidators.eitherOrderNumberOrDatesValidator,
                ],
            }
        );
    }

    getDateRangeFormGroup(
        formGroup: 'createDate' | 'desiredShipDate'
    ): FormGroup {
        const control = this.searchForm.get(formGroup);
        return control instanceof FormGroup ? control : null;
    }

    get enableSubmit(): boolean {
        return this.searchForm.errors == null || this.searchForm.valid;
    }

    get enableReset(): boolean {
        return this.totalFieldsInformed() > 0;
    }

    // Reseting Filters
    resetFilters(): void {
        this.searchForm.reset();
        Object.keys(this.searchForm.controls).forEach((filterKey) => {
            this.searchForm.controls[filterKey].enable();
        });
        this.emitNoResults();
    }

    // Apply Filters
    applyFilterSearch(): void {
        this.emitResults(this.searchForm.value);
    }

    toggleFilter(toggleFlag: boolean): void {
        this.toggleFilters.emit(toggleFlag);
    }

    emitResults(value?: SearchOrderFilterDTO) {
        this.totalFieldsApplied = this.totalFieldsInformed();
        this.onApplySearchFilters.emit(value);
    }

    emitNoResults() {
        this.totalFieldsApplied = this.totalFieldsInformed();
        this.onResetFilters.emit(null);
    }
}
