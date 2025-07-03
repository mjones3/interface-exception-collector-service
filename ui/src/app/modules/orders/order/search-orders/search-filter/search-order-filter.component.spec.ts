import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { provideMockStore } from '@ngrx/store/testing';
import { Apollo } from 'apollo-angular';
import moment from 'moment';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';
import { OrderService } from '../../../services/order.service';
import { SearchOrderFilterComponent } from './search-order-filter.component';
import { AuthState } from 'app/core/state/auth/auth.reducer';
import { ApolloError } from '@apollo/client';

const SINGLE_SEARCH_FILTER_KEYS: string[] = ['orderNumber'];

describe('SearchOrderFilterComponent', () => {
    let component: SearchOrderFilterComponent;
    let fixture: ComponentFixture<SearchOrderFilterComponent>;
    let orderService: OrderService;
    let toastr: ToastrService;

    const initialState: AuthState = {
        id: 'mock-user-id',
        loaded: true,
    };

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
                provideMockStore({initialState }),
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
                ToastrService,
            ],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchOrderFilterComponent);
        component = fixture.componentInstance;
        orderService = TestBed.inject(OrderService);
        toastr = TestBed.inject(ToastrService);
        jest.spyOn(orderService, 'searchOrderCriteria').mockReturnValue(of());
        jest.spyOn(toastr, 'error');
        component.ngOnInit();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
        expect(component.enableSubmit).toBeFalsy();
    });

    

    it('should handle error in loadCriteriaOptions', () => {
        const error = new ApolloError({ graphQLErrors: [new Error('Test error')] });
        jest.spyOn(orderService, 'searchOrderCriteria').mockReturnValue(throwError(() => error));
        
        component.ngOnInit();
        
        expect(toastr.error).toHaveBeenCalledWith('Something went wrong.');
    });

    it('should properly handle shipmentType selection', () => {
        const shipmentType = 'STANDARD';
        component.searchForm.get('shipmentType').setValue(shipmentType);
        expect(component.shipmentTypeSelected()).toBe(shipmentType);
    });

    it('should disable both customers and shipToLocation fields when shipmentType is empty', () => {
        component.searchForm.get('shipmentType').setValue('customer');
        component.searchForm.get('shipmentType').setValue('');
        fixture.detectChanges();

        const customersControl = component.searchForm.get('customers');
        const shipToLocationControl = component.searchForm.get('shipToLocation');
        
        expect(customersControl.disabled).toBeTruthy();
        expect(shipToLocationControl.disabled).toBeTruthy();
        expect(component.shipmentTypeSelected()).toBe('');
    });

    it('should properly get date range form groups', () => {
        const createDateGroup = component.getDateRangeFormGroup('createDate');
        expect(createDateGroup).toBeTruthy();
        expect(createDateGroup.get('start')).toBeTruthy();
        expect(createDateGroup.get('end')).toBeTruthy();

        const desiredShipDateGroup = component.getDateRangeFormGroup('desiredShipDate');
        expect(desiredShipDateGroup).toBeTruthy();
        expect(desiredShipDateGroup.get('start')).toBeTruthy();
        expect(desiredShipDateGroup.get('end')).toBeTruthy();
    });


    it('should properly handle form state when toggling between order number and other filters', () => {
        component.searchForm.get('orderNumber').setValue('TEST123');
        expect(component.searchForm.get('createDate').disabled).toBeTruthy();
        expect(component.searchForm.get('orderStatus').disabled).toBeTruthy();

        component.searchForm.get('orderNumber').setValue('');
        component.searchForm.get('orderStatus').setValue(['PENDING']);
        expect(component.searchForm.get('orderNumber').disabled).toBeTruthy();
        expect(component.searchForm.get('createDate').enabled).toBeTruthy();
        expect(component.searchForm.get('orderStatus').enabled).toBeTruthy();

        component.resetFilters();
        expect(component.searchForm.get('orderNumber').enabled).toBeTruthy();
        expect(component.searchForm.get('createDate').enabled).toBeTruthy();
        expect(component.searchForm.get('orderStatus').enabled).toBeTruthy();
    });

    it('should clear form when reset is triggered', () => {
        const { createDate, desiredShipDate, ...nonDateRangeFields } =
            component.searchForm.controls;

        expect(component.enableSubmit).toBeFalsy();

        Object.keys(nonDateRangeFields).forEach((filterKey) => {
            component.searchForm.controls[filterKey].setValue('Test');
        });
        expect(component.enableSubmit).toBeTruthy();

        component.resetFilters();

        Object.keys(nonDateRangeFields).forEach((filterKey) => {
            expect(component.searchForm.controls[filterKey].value).toBe(null);
        });

        expect(component.enableSubmit).toBeFalsy();

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
            shipmentType: '',
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

    it('should not be able to select create date parameters values greater than current date', () => {
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        const tomorrowDate = moment(tomorrow).format('MM/DD/YYYY');

        component.searchForm.controls['createDate'].setValue({
            start: tomorrowDate,
            end: tomorrowDate,
        });
        component.searchForm.controls['createDate'].updateValueAndValidity();
        expect(component.searchForm.valid).toBeFalsy();
        expect(component.enableSubmit).toBeFalsy();
    });

    it('should be able to multi-select options for Priority, Status, and Ship to Customer fields', () => {
        component.searchForm.controls['orderStatus'].setValue([
            'Option 1',
            'Option 2',
            'Option 3',
        ]);
        const statuses = component.searchForm.controls[
            'orderStatus'
        ].getRawValue() as string[];
        expect(statuses).toHaveLength(3);

        component.searchForm.controls['customers'].setValue([
            'Customer 2',
            'Customer 3',
        ]);
        const customers = component.searchForm.controls[
            'customers'
        ].getRawValue() as string[];
        expect(customers).toHaveLength(2);
    });

    it('should be able to see order number disabled when filtering by remaining filter fields', () => {
        const { createDate, desiredShipDate, ...nonDateRangeFields } =
            component.searchForm.controls;

        Object.keys(nonDateRangeFields).forEach((filterKey) => {
            component.resetFilters();
            expect(component.searchForm.controls['orderNumber'].enabled).toBe(
                true
            );
            component.searchForm.controls[filterKey].setValue('Test');
            if (filterKey != 'orderNumber') {
                expect(
                    component.searchForm.controls['orderNumber'].enabled
                ).toBe(false);
            }
        });

        component.resetFilters();
        expect(component.searchForm.controls['orderNumber'].enabled).toBe(true);
        component.searchForm.controls['createDate'].setValue({
            start: '01/01/2000',
            end: '03/01/2024',
        });
        expect(component.searchForm.controls['orderNumber'].enabled).toBe(
            false
        );
    });

    it('should see the number of fields used to select the filter criteria', () => {
        const { createDate, desiredShipDate, customers, shipToLocation,  ...otherFields } =
            component.searchForm.controls;

        let filterCount = 0;
        component.resetFilters();
        Object.keys(otherFields).forEach((filterKey) => {
            if (filterKey != 'orderNumber') {
                component.searchForm.controls[filterKey].setValue('Test');
                filterCount++;
                expect(component.totalFieldsInformed()).toBe(filterCount);
            }
        });
    });
});
