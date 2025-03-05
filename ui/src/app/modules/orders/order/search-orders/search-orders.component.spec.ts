import { provideHttpClient } from '@angular/common/http';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { MatIconTestingModule } from '@angular/material/icon/testing';
import { Router, RouterModule } from '@angular/router';
import { ApolloQueryResult } from '@apollo/client';
import { TranslateModule } from '@ngx-translate/core';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { PageDTO } from '../../models/page.model';
import { OrderReportDTO } from '../../models/search-order.model';
import { OrderService } from '../../services/order.service';
import { SearchOrdersComponent } from './search-orders.component';

describe('SearchOrdersComponent', () => {
    let component: SearchOrdersComponent;
    let fixture: ComponentFixture<SearchOrdersComponent>;
    let router: Router;
    let orderService: jest.Mocked<OrderService>;
    let toaster: jest.Mocked<ToastrService>;

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
            ],
        });
        fixture = TestBed.createComponent(SearchOrdersComponent);
        router = TestBed.inject(Router);
        orderService = TestBed.inject(
            OrderService
        ) as jest.Mocked<OrderService>;
        toaster = TestBed.inject(ToastrService) as jest.Mocked<ToastrService>;
        component = fixture.componentInstance;
    }));

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should navigate search order detial page', () => {
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

        expect(component.loading).toBe(false);
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
        expect(component.loading).toBe(false);
        expect(component.page().content).toEqual(emptyResponse);
        expect(component.details).not.toHaveBeenCalled();
        expect(toaster.warning).not.toHaveBeenCalled();
        expect(toaster.error).not.toHaveBeenCalled();
    });
});
