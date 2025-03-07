import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { Apollo } from 'apollo-angular';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { OrderService } from '../../../services/order.service';
import { SearchOrderFilterComponent } from './search-order-filter.component';

const SINGLE_SEARCH_FILTER_KEYS: string[] = ['orderNumber'];

describe('SearchOrderFilterComponent', () => {
    let component: SearchOrderFilterComponent;
    let fixture: ComponentFixture<SearchOrderFilterComponent>;
    let orderService: OrderService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SearchOrderFilterComponent,
                NoopAnimationsModule,
                MatNativeDateModule,
                ToastrModule.forRoot(),
            ],
            providers: [
                provideHttpClient(),
                provideMockStore({}),
                Apollo,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            data: {
                                searchData: {},
                            },
                        },
                    },
                },
                OrderService,
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchOrderFilterComponent);
        component = fixture.componentInstance;
        orderService = TestBed.inject(OrderService);
        jest.spyOn(orderService, 'searchOrderCriteria').mockReturnValue(of());
        component.ngOnInit();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should clear form when reset is triggered', () => {
        const { createDate, desiredShipDate, ...nonDateRangeFields } =
            component.searchForm.controls;

        Object.keys(nonDateRangeFields).forEach((filterKey) => {
            component.searchForm.controls[filterKey].setValue('Test');

            component.resetFilters();

            expect(component.searchForm.controls[filterKey].value).toBe(null);
            expect(component.enableSubmit).toBeFalsy();
        });
        [createDate, desiredShipDate].forEach((dateRangeField) => {
            dateRangeField.setValue({ start: '01/01/2024', end: '01/01/2024' });
            component.resetFilters();
            expect(dateRangeField.value?.start).toBe(null);
            expect(dateRangeField.value?.end).toBe(null);
        });
    });

    it('should disable apply button', () => {
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should enable apply button when order number is entered', () => {
        component.searchForm.controls['orderNumber'].setValue('Test');
        expect(component.enableSubmit).toBeTruthy();
    });

    it('should enable apply button when create date is entered', () => {
        component.searchForm.controls['orderNumber'].setValue('');
        component.searchForm.controls['createDate'].setValue({
            start: '01/01/2024',
            end: '03/01/2024',
        });
        component.searchForm.controls['createDate'].updateValueAndValidity();
        expect(component.enableSubmit).toBeTruthy();
    });

    it('should disable apply button when invalid create date is entered', () => {
        component.searchForm.controls['createDate'].setValue({
            start: '01/mm/2000',
            end: '03/01/2024',
        });
        component.searchForm.controls['createDate'].updateValueAndValidity();
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should keep only single search field enabled', () => {
        SINGLE_SEARCH_FILTER_KEYS.forEach((singleFilter) => {
            component.resetFilters();
            component.searchForm.controls[singleFilter].setValue('Test');
            expect(component.enableSubmit).toBeTruthy();
        });
    });

    it('should show filters', () => {
        const toggleFiltersEvent = jest.spyOn(component.toggleFilters, 'emit');

        component.toggleFilter(true);

        expect(toggleFiltersEvent).toHaveBeenNthCalledWith(1, true);
    });

    it('should hide filters', () => {
        const toggleFiltersEvent = jest.spyOn(component.toggleFilters, 'emit');

        component.toggleFilter(false);

        expect(toggleFiltersEvent).toHaveBeenNthCalledWith(1, false);
    });

    it('should apply filters', () => {
        const applySearchFiltersEvent = jest.spyOn(
            component.onApplySearchFilters,
            'emit'
        );
        const expectedValue = {
            orderNumber: '',
            orderStatus: '',
            customers: '',
            deliveryTypes: '',
            createDate: { start: null, end: null },
            desiredShipDate: { start: null, end: null },
        };

        component.applyFilterSearch();

        expect(applySearchFiltersEvent).toHaveBeenNthCalledWith(
            1,
            expectedValue
        );
    });
});
