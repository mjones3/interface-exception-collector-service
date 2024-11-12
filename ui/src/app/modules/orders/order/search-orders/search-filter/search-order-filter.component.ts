import {
    animate,
    state,
    style,
    transition,
    trigger,
} from '@angular/animations';
import { CommonModule, NgTemplateOutlet } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
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
import { ActivatedRoute } from '@angular/router';
import { AutoUnsubscribe, SelectOptionDto } from '@shared';
import { Subject, Subscription, debounceTime } from 'rxjs';
import { DateRangePickerComponent } from '../../../../../shared/components/date-range-picker/date-range-picker.component';
import { FiltersComponent } from '../../../../../shared/components/filters/filters.component';
import { MultipleSelectComponent } from '../../../../../shared/components/multiple-select/multiple-select.component';
import { SelectAllDirective } from '../../../../../shared/directive/select-all/select-all.directive';
import { BioproValidators } from '../../../../../shared/forms/biopro-validators';
import {
    SearchOrderFilterDTO,
    SearchOrderResolverData,
} from '../../../models/order.dto';

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
        NgTemplateOutlet,
        SelectAllDirective,
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

    resolverData: SearchOrderResolverData;
    searchForm: FormGroup;
    singleSearchInputChanged: Subject<string> = new Subject<string>();
    multiSearchInputChanged: Subject<string> = new Subject<string>();
    subscription$1: Subscription;
    subscription$2: Subscription;
    private appliedTotalFilterCount = 0;
    private locationTypesOrdered = false;

    constructor(
        private formBuilder: FormBuilder,
        private activatedRoute: ActivatedRoute
    ) {}

    ngOnInit(): void {
        this.appliedTotalFilterCount = 0;

        this.initDataFromResolver();

        this.initForm();

        this.subscription$1 = this.singleSearchInputChanged
            .pipe(debounceTime(DEBOUNCE_TIME))
            .subscribe((res) => {
                this.onChangeSingleSearchFilter(res);
            });
        this.subscription$2 = this.multiSearchInputChanged
            .pipe(debounceTime(DEBOUNCE_TIME))
            .subscribe((res) => {
                this.onChangeMultipleSearchFilter(res);
            });
    }

    private initDataFromResolver(): void {
        this.resolverData = this.activatedRoute.snapshot.data?.searchData;
    }

    private initForm(): void {
        this.searchForm = this.formBuilder.group(
            {
                orderNumber: ['', [Validators.maxLength(25)]],
                orderStatus: [''],
                orderPriority: [''],
                customer: [''],
                createDateFrom: ['', [Validators.required]],
                createDateTo: ['', [Validators.required]],
                desiredShipmentDateFrom: [''],
                desiredShipmentDateTo: [''],
            },
            { validators: BioproValidators.hasAtLeastOne }
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

        this.applySearchFilters.emit(this.removeSelectAllIndicator());
    }

    private removeSelectAllIndicator(): SearchOrderFilterDTO {
        return this.searchForm.value;
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

    // disable all single search filters when multi search filters are active
    onChangeMultipleSearchFilter(key: string): void {
        if (this.searchForm.controls[key].value) {
            if (this.searchForm.controls[key].value.toString().length >= 1) {
                SINGLE_SEARCH_FILTER_KEYS.forEach((filterKey) => {
                    this.searchForm.controls[filterKey].setValue(null);
                    this.searchForm.controls[filterKey].disable();
                });
            }
        } else if (!this.searchForm.valid) {
            Object.keys(this.searchForm.controls).forEach((filterKey) => {
                this.searchForm.controls[filterKey].enable();
            });
        }
        this.searchForm.updateValueAndValidity();
    }

    get appliedFiltersCounter(): number {
        return this.appliedTotalFilterCount;
    }

    selectOptionKeys(selectOptions: SelectOptionDto[]): string[] {
        return selectOptions?.length > 0
            ? selectOptions.map((option) => option.optionKey)
            : [];
    }

    search(source: SelectOptionDto[], filterValue: string): SelectOptionDto[] {
        if (!filterValue || filterValue === '') {
            return source;
        }
        return source.filter((optionValue) =>
            optionValue.optionDescription
                .toLowerCase()
                .includes(filterValue.toLowerCase())
        );
    }
}
