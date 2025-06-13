import { provideHttpClient } from '@angular/common/http';
import {
    ComponentFixture,
    fakeAsync,
    TestBed,
    waitForAsync,
} from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { Router, RouterModule } from '@angular/router';
import { ApolloQueryResult } from '@apollo/client';
import { TranslateModule } from '@ngx-translate/core';
import { PageDTO } from 'app/shared/models/page.model';
import { CookieService } from 'ngx-cookie-service';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { OrderReportDTO } from '../../models/search-order.model';
import { OrderService } from '../../services/order.service';
import { SearchOrdersComponent } from './search-orders.component';

describe('SearchOrdersComponent', () => {
    let component: SearchOrdersComponent;
    let fixture: ComponentFixture<SearchOrdersComponent>;
    let router: Router;
    let orderService: jest.Mocked<OrderService>;
    let toaster: jest.Mocked<ToastrService>;
    let cookieService: jest.Mocked<CookieService>;

    const content: OrderReportDTO[] = [
        {
            orderId: 103,
            orderNumber: 69,
            externalId: 'ORDER005',
            orderStatus: 'IN_PROGRESS',
            createDate: '2024-12-13T05:00:00.000Z',
            desireShipDate: '2024-08-20',
            orderPriorityReport: {
                priority: 'STAT',
                priorityColor: '#ff3333',
            },
            orderCustomerReport: {
                code: 'A1235',
                name: 'Creative Testing Solutions',
            },
        },
        {
            orderId: 87,
            orderNumber: 53,
            externalId: 'EXT11',
            orderStatus: 'OPEN',
            createDate: '2024-12-13T05:00:00.000Z',
            desireShipDate: '2024-12-25',
            orderPriorityReport: {
                priority: 'STAT',
                priorityColor: '#ff3333',
            },
            orderCustomerReport: {
                code: 'A1235',
                name: 'Creative Testing Solutions',
            },
        },
    ];
    const page: PageDTO<OrderReportDTO> = {
        content,
        pageNumber: 0,
        pageSize: 20,
        totalRecords: 2,
        querySort: null,
        hasPrevious: false,
        hasNext: false,
        isFirst: true,
        isLast: true,
        totalPages: 1,
    };

    beforeEach(waitForAsync(() => {
        const orderServiceSpy = {
            searchOrders: jest.fn(),
        };
        const toasterSpy = {
            warning: jest.fn(),
            error: jest.fn(),
        };
        const cookieServiceSpy = {
            get: jest.fn(),
        };
        TestBed.configureTestingModule({
            imports: [
                SearchOrdersComponent,
                ToastrModule.forRoot(),
                MatIconTestingModule,
                RouterModule.forRoot([]),
                MatNativeDateModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                provideHttpClient(),
                OrderService,
                { provide: OrderService, useValue: orderServiceSpy },
                { provide: ToastrService, useValue: toasterSpy },
                { provide: CookieService, useValue: cookieServiceSpy },
            ],
        });
        fixture = TestBed.createComponent(SearchOrdersComponent);
        router = TestBed.inject(Router);
        orderService = TestBed.inject(
            OrderService
        ) as jest.Mocked<OrderService>;
        toaster = TestBed.inject(ToastrService) as jest.Mocked<ToastrService>;
        cookieService = TestBed.inject(
            CookieService
        ) as jest.Mocked<CookieService>;
        component = fixture.componentInstance;
    }));

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should navigate search order detail page', () => {
        const orderId = 1;
        jest.spyOn(router, 'navigateByUrl');
        component.details(orderId);
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            `/orders/${orderId}/order-details`
        );
    });

    it('should fetch search orders successfully', () => {
        const response: ApolloQueryResult<{
            searchOrders: PageDTO<OrderReportDTO>;
        }> = {
            data: { searchOrders: page },
            loading: false,
            networkStatus: 7,
        };

        orderService.searchOrders.mockReturnValue(of(response));

        jest.spyOn(component, 'details').mockImplementation();
        jest.spyOn(component, 'isFilterApplied').mockReturnValue(true);

        component.searchOrders();

        expect(component.loading()).toBe(false);
        expect(component.page()).toEqual(page);
        expect(toaster.warning).not.toHaveBeenCalled();
        expect(toaster.error).not.toHaveBeenCalled();
    });

    it('should handle search orders with empty response', () => {
        const emptyResponse: OrderReportDTO[] = [];
        const response: ApolloQueryResult<{
            searchOrders: PageDTO<OrderReportDTO>;
        }> = {
            data: {
                searchOrders: {
                    content: emptyResponse,
                    pageNumber: 0,
                    pageSize: 20,
                    totalRecords: 2,
                    querySort: null,
                    hasPrevious: false,
                    hasNext: false,
                    isFirst: true,
                    isLast: true,
                    totalPages: 1,
                },
            },
            loading: false,
            networkStatus: 7,
        };
        orderService.searchOrders.mockReturnValue(of(response));
        jest.spyOn(component, 'details').mockImplementation();
        jest.spyOn(component, 'isFilterApplied').mockReturnValue(true);
        component.searchOrders();
        expect(component.loading()).toBe(false);
        expect(component.page().content).toEqual(emptyResponse);
        expect(component.details).not.toHaveBeenCalled();
        expect(toaster.warning).not.toHaveBeenCalled();
        expect(toaster.error).not.toHaveBeenCalled();
    });

    it('should keep search criterias throughout pagination', () => {
        const response: ApolloQueryResult<{
            searchOrders: PageDTO<OrderReportDTO>;
        }> = {
            data: {
                searchOrders: page,
            },
            loading: false,
            networkStatus: 7,
        };
        component.currentFilter = {
            customers: ['Customer A', 'Customer B'],
        };

        orderService.searchOrders.mockReturnValue(of(response));
        cookieService.get.mockReturnValue('123456789');

        // Page 0 navigation
        component.handlePagination({
            pageIndex: 0,
            pageSize: 20,
            length: 100,
        });
        expect(orderService.searchOrders).toBeCalledWith({
            locationCode: '123456789',
            customers: ['Customer A', 'Customer B'], // Original criteria
            pageNumber: 0,
            pageSize: 20,
        });

        // Page 1 navigation
        component.handlePagination({
            previousPageIndex: 0,
            pageIndex: 1,
            pageSize: 20,
            length: 100,
        });
        expect(orderService.searchOrders).toBeCalledWith({
            locationCode: '123456789',
            customers: ['Customer A', 'Customer B'], // Should keep original criteria
            pageNumber: 1,
            pageSize: 20,
        });
    });

    it('should keep search criteria throughout sorting', () => {
        const response: ApolloQueryResult<{
            searchOrders: PageDTO<OrderReportDTO>;
        }> = {
            data: {
                searchOrders: page,
            },
            loading: false,
            networkStatus: 7,
        };
        component.currentFilter = {
            customers: ['Customer A', 'Customer B'],
        };

        orderService.searchOrders.mockReturnValue(of(response));
        cookieService.get.mockReturnValue('123456789');

        // Sorting ASC
        component.handleSorting({
            active: 'activeProperty',
            direction: 'asc',
        });
        expect(orderService.searchOrders).toBeCalledWith({
            locationCode: '123456789',
            customers: ['Customer A', 'Customer B'], // Original criteria
            pageSize: 20,
            querySort: {
                orderByList: [
                    {
                        property: 'activeProperty',
                        direction: 'ASC',
                    },
                ],
            },
        });

        // Sorting DESC
        component.handleSorting({
            active: 'activeProperty',
            direction: 'desc',
        });
        expect(orderService.searchOrders).toBeCalledWith({
            locationCode: '123456789',
            customers: ['Customer A', 'Customer B'], // Original criteria
            pageSize: 20,
            querySort: {
                orderByList: [
                    {
                        property: 'activeProperty',
                        direction: 'DESC',
                    },
                ],
            },
        });
    });

    it('Should be redirected to order details page if only one record is found', fakeAsync(() => {
        const singleContent: OrderReportDTO[] = [
            {
                orderId: 103,
                orderNumber: 69,
                externalId: 'ORDER005',
                orderStatus: 'IN_PROGRESS',
                createDate: '2024-12-13T05:00:00.000Z',
                desireShipDate: '2024-08-20',
                orderPriorityReport: {
                    priority: 'STAT',
                    priorityColor: '#ff3333',
                },
                orderCustomerReport: {
                    code: 'A1235',
                    name: 'Creative Testing Solutions',
                },
            },
        ];
        const singlePage: PageDTO<OrderReportDTO> = {
            content: singleContent,
            pageNumber: 0,
            pageSize: 20,
            totalRecords: 1,
            querySort: null,
            hasPrevious: false,
            hasNext: false,
            isFirst: true,
            isLast: true,
            totalPages: 1,
        };

        const response: ApolloQueryResult<{
            searchOrders: PageDTO<OrderReportDTO>;
        }> = {
            data: { searchOrders: singlePage },
            loading: false,
            networkStatus: 7,
        };

        orderService.searchOrders.mockReturnValue(of(response));

        jest.spyOn(component, 'isFilterApplied').mockReturnValue(true);
        jest.spyOn(router, 'navigateByUrl').mockImplementation();
        cookieService.get.mockReturnValue('123456789');

        component.searchOrders();

        const orderId = 103; // defined inside singleContent object
        expect(router.navigateByUrl).toHaveBeenCalledWith(
            `/orders/${orderId}/order-details`
        );
    }));

    it('should be able to reset the applied filter criteria', () => {
        component.currentFilter = {
            customers: ['Customer A', 'Customer B'],
            createDate: {
                start: '0000-00-00',
                end: '0000-00-00',
            },
            deliveryTypes: ['Type A', 'Type B'],
            limit: 5,
            sortBy: 'criteria',
            page: 0,
            orderNumber: '1',
            orderStatus: ['status'],
            order: '1',
            desiredShipDate: {
                start: '0000-00-00',
                end: '0000-00-00',
            },
        };

        expect(component.currentFilter.customers).toBeTruthy();
        expect(component.currentFilter.createDate).toBeTruthy();
        expect(component.currentFilter.deliveryTypes).toBeTruthy();
        expect(component.currentFilter.limit).toBeTruthy();
        expect(component.currentFilter.sortBy).toBeTruthy();
        expect(component.currentFilter.orderNumber).toBeTruthy();
        expect(component.currentFilter.orderStatus).toBeTruthy();
        expect(component.currentFilter.order).toBeTruthy();
        expect(component.currentFilter.desiredShipDate).toBeTruthy();

        component.resetFilterSearch();

        expect(component.currentFilter.customers).toBeFalsy();
        expect(component.currentFilter.createDate).toBeFalsy();
        expect(component.currentFilter.deliveryTypes).toBeFalsy();
        expect(component.currentFilter.limit).toBeFalsy();
        expect(component.currentFilter.sortBy).toBeFalsy();
        expect(component.currentFilter.orderNumber).toBeFalsy();
        expect(component.currentFilter.orderStatus).toBeFalsy();
        expect(component.currentFilter.order).toBeFalsy();
        expect(component.currentFilter.desiredShipDate).toBeFalsy();
    });
});
