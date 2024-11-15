import {
    animate,
    state,
    style,
    transition,
    trigger,
} from '@angular/animations';
import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { ApolloError } from '@apollo/client';
import { TranslateService } from '@ngx-translate/core';
import { AutoUnsubscribe, SelectOptionDto } from '@shared';
import { ToastrService } from 'ngx-toastr';
import { DateRangePickerComponent } from '../../../../../shared/components/date-range-picker/date-range-picker.component';
import { FiltersComponent } from '../../../../../shared/components/filters/filters.component';
import { MultipleSelectComponent } from '../../../../../shared/components/multiple-select/multiple-select.component';
import { BioproValidators } from '../../../../../shared/forms/biopro-validators';
import { SearchOrderFilterDTO } from '../../../models/order.dto';
import { OrderService } from '../../../services/order.service';

const SINGLE_SEARCH_FILTER_KEYS: string[] = ['orderNumber'];
const DEBOUNCE_TIME = 100;

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
    @Input() showFilters = false;
    @Output() applySearchFilters: EventEmitter<SearchOrderFilterDTO> =
        new EventEmitter<SearchOrderFilterDTO>();
    @Output() toggleFilters: EventEmitter<boolean> =
        new EventEmitter<boolean>();

    searchForm: FormGroup;

    private appliedTotalFilterCount = 0;
    statusOptions: SelectOptionDto[];
    priorityOptions: SelectOptionDto[];
    customers: SelectOptionDto[];

    today = new Date();

    constructor(
        private formBuilder: FormBuilder,
        private orderService: OrderService,
        private toaster: ToastrService,
        private translateService: TranslateService
    ) {}

    ngOnInit(): void {
        this.appliedTotalFilterCount = 0;

        this.loadCriteriaOptions();

        this.initForm();
        this.today.setHours(0, 0, 0, 0);
    }

    private loadCriteriaOptions() {
        this.orderService.searchOrderCriteria().subscribe({
            next: (response) => {
                this.statusOptions =
                    response.data.searchOrderCriteria.orderStatus.map(
                        (item) => ({
                            optionKey: item.optionValue,
                            optionDescription: this.translateService.instant(
                                item.descriptionKey
                            ),
                        })
                    );
                this.priorityOptions =
                    response.data.searchOrderCriteria.orderPriorities.map(
                        (item) => ({
                            optionKey: item.optionValue,
                            optionDescription: this.translateService.instant(
                                item.descriptionKey
                            ),
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
        this.searchForm = this.formBuilder.group(
            {
                orderNumber: ['' /*, [Validators.maxLength(25)] */],
                orderStatus: [''],
                deliveryTypes: [''],
                customers: [''],
                createDateFrom: [''],
                createDateTo: ['', []],
                desiredShipDateFrom: [''],
                desiredShipDateTo: [''],
            },
            {
                validators: [BioproValidators.hasAtLeastOne],
            }
        );
    }

    get enableSubmit(): boolean {
        return this.searchForm.valid;
    }

    // Reseting Filters
    resetFilters(): void {
        this.appliedTotalFilterCount = 0;
        this.searchForm.reset();
        Object.keys(this.searchForm.controls).forEach((filterKey) => {
            this.searchForm.controls[filterKey].enable();
        });
        this.applySearchFilters.emit({});
    }

    // Apply Filters
    applyFilterSearch(): void {
        this.appliedTotalFilterCount = Object.values(
            this.searchForm.value
        ).filter((ele) => null != ele && '' != ele).length;

        this.applySearchFilters.emit(this.searchForm.value);
    }

    toggleFilter(toggleFlag: boolean): void {
        this.toggleFilters.emit(toggleFlag);
    }

    // disable all other filters when a single search filter is active
    onChangeSingleSearchFilter(key: string): void {
        if (this.searchForm.controls[key].value) {
            if (this.searchForm.controls[key].value.toString().length >= 1) {
                Object.keys(this.searchForm.controls).forEach((filterKey) => {
                    if (key !== filterKey) {
                        this.searchForm.controls[filterKey].setValue(null);
                        this.searchForm.controls[filterKey].disable();
                    }
                });
            }
        } else {
            Object.keys(this.searchForm.controls).forEach((filterKey) => {
                this.searchForm.controls[filterKey].enable();
            });
        }
        this.searchForm.updateValueAndValidity();
    }

    get appliedFiltersCounter(): number {
        return this.appliedTotalFilterCount;
    }
}
