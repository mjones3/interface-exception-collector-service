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
    EventEmitter,
    Input,
    OnInit,
    Output,
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

    @Input() showFilters = false;
    @Output() applySearchFilters: EventEmitter<SearchOrderFilterDTO> =
        new EventEmitter<SearchOrderFilterDTO>();
    @Output() toggleFilters: EventEmitter<boolean> =
        new EventEmitter<boolean>();

    searchForm: FormGroup;

    statusOptions: SelectOptionDto[];
    priorityOptions: SelectOptionDto[];
    customers: SelectOptionDto[];

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
                this.searchForm
                    .get('orderNumber')
                    ?.removeValidators(Validators.required);
                Object.keys(this.searchForm.controls).forEach((key) => {
                    if (key !== 'orderNumber') {
                        this.searchForm.get(key)?.enable({ emitEvent: false });
                        if (key === 'createDate') {
                            this.searchForm
                                .get(key)
                                .get('start')
                                ?.addValidators(Validators.required);
                        }
                    }
                });
            } else if (this.orderNumberInformed()) {
                this.searchForm
                    .get('orderNumber')
                    ?.enable({ emitEvent: false });
                this.searchForm
                    .get('orderNumber')
                    ?.setValidators(Validators.required);
                Object.keys(this.searchForm.controls).forEach((key) => {
                    if (key !== 'orderNumber') {
                        this.searchForm.get(key)?.disable({ emitEvent: false });
                        if (key === 'createDate') {
                            this.searchForm
                                .get(key)
                                .get('start')
                                ?.removeValidators(Validators.required);
                        }
                    }
                });
            } else {
                Object.keys(this.searchForm.controls).forEach((key) => {
                    this.searchForm.get(key)?.enable({ emitEvent: false });
                    if (key === 'orderNumber') {
                        this.searchForm
                            .get(key)
                            ?.addValidators(Validators.required);
                    } else if (key === 'createDate') {
                        this.searchForm
                            .get(key)
                            ?.get('start')
                            ?.addValidators(Validators.required);
                    }
                });
            }
        });
    }

    orderNumberInformed = () => this.isFieldInformed('orderNumber');

    remainingFieldsInformed() {
        const { orderNumber, ...otherFields } = this.searchForm.value;

        return Object.keys(otherFields).some((key) =>
            this.isFieldInformed(key)
        );
    }

    isDateRangeInformed = (date) =>
        date != null && (date?.start != null || date?.end != null);

    totalFieldsInformed = () =>
        Object.keys(this.searchForm.value).filter((key) =>
            this.isFieldInformed(key)
        ).length;

    isFieldInformed(key: string) {
        if (key === 'createDate' || key === 'desiredShipDate') {
            return this.isDateRangeInformed(this.searchForm.value[key]);
        } else if (Array.isArray(this.searchForm.value[key])) {
            return this.searchForm.value[key].length > 0;
        } else {
            return (
                this.searchForm.value[key] != null &&
                this.searchForm.value[key] !== ''
            );
        }
        return false;
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
                this.customers =
                    response.data.searchOrderCriteria.customers.map((item) => ({
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
                orderNumber: [
                    '',
                    [Validators.required, Validators.maxLength(25)],
                ],
                orderStatus: [''],
                deliveryTypes: [''],
                customers: [''],
                createDate: this.formBuilder.group({
                    start: [null, [Validators.required]], // Start date
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
        return !Object.keys(this.searchForm.controls).every((key) => {
            const controlValue = this.searchForm.get(key)?.value;
            return (
                controlValue === '' ||
                controlValue === null ||
                (Array.isArray(controlValue) && controlValue.length === 0)
            );
        });
    }

    // Reseting Filters
    resetFilters(): void {
        this.searchForm.reset();
        Object.keys(this.searchForm.controls).forEach((filterKey) => {
            this.searchForm.controls[filterKey].enable();
        });
        this.emitResults({});
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
        this.applySearchFilters.emit(value);
    }

    protected readonly FormGroup = FormGroup;
}
