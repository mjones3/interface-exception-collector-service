import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMomentDateModule } from '@angular/material-moment-adapter';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  ControlErrorsDirective,
  getAppInitializerMockProvider,
  LookUpDto,
  OrderService,
  OrderSummaryDto,
  ProcessHeaderComponent,
  ProcessProductDto,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { OrderStatuses, OrderSummary, ShipmentType } from '@rsa/distribution/core/models/orders.model';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { orderFieldsMock } from '@rsa/distribution/data/mock/orders.mock';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { CalendarModule } from 'primeng/calendar';
import { MultiSelectModule } from 'primeng/multiselect';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { SearchOrdersComponent } from './search-orders.component';

const ORDER_STATUS_DESCRIPTION_KEY = 'order_status.label';
const ORDER_STATUS_OPTION_VALUE = 'OPEN';
const ORDER_DELIVERY_TYPE_DESCRIPTION_KEY = 'order_delivery_type.label';
const ORDER_DELIVERY_TYPE_OPTION_VALUE = 'order_delivery_type_option_value';
const orderStatuses = OrderStatuses;

describe('SearchOrdersComponent', () => {
  let component: SearchOrdersComponent;
  let fixture: ComponentFixture<SearchOrdersComponent>;
  let orderService: OrderService;
  let toaster: ToastrService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [SearchOrdersComponent, ProcessHeaderComponent, ValidationPipe, ControlErrorsDirective],
      imports: [
        HttpClientTestingModule,
        NoopAnimationsModule,
        RouterTestingModule,
        FormsModule,
        ReactiveFormsModule,
        TreoCardModule,
        TableModule,
        MultiSelectModule,
        MaterialModule,
        CalendarModule,
        ButtonModule,
        RippleModule,
        MatDividerModule,
        MatAutocompleteModule,
        MatDatepickerModule,
        MatFormFieldModule,
        MatMomentDateModule,
        MatSelectModule,
        MatIconModule,
        MatInputModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...toasterMockProvider,
        ...getAppInitializerMockProvider('distribution-app'),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                seachOrderConfigData: [
                  {
                    body: <LookUpDto[]>[
                      {
                        id: 1,
                        active: true,
                        descriptionKey: ORDER_STATUS_DESCRIPTION_KEY,
                        type: 'ORDER_STATUS',
                        optionValue: ORDER_STATUS_OPTION_VALUE,
                      },
                      {
                        id: 2,
                        active: true,
                        descriptionKey: ORDER_DELIVERY_TYPE_DESCRIPTION_KEY,
                        type: 'ORDER_DELIVERY_TYPE',
                        optionValue: ORDER_DELIVERY_TYPE_OPTION_VALUE,
                      },
                      {
                        id: 3,
                        active: true,
                        descriptionKey: 'order-shipment-type-customer.label',
                        type: 'ORDER_SHIPMENT_TYPE',
                        optionValue: ShipmentType.CUSTOMER,
                      },
                    ],
                  },
                  {
                    body: <ProcessProductDto>{
                      id: '1',
                      active: true,
                      descriptionKey: 'order_process.label',
                      orderNumber: 1,
                      properties: new Map<string, string>([[orderStatuses.OPEN, '#fffff']]),
                    },
                  },
                ],
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<SearchOrdersComponent>(SearchOrdersComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    orderService = TestBed.inject(OrderService);
    toaster = TestBed.inject(ToastrService);
    router = TestBed.inject(Router);
    addRsaIconsMock(testContext);

    fixture.detectChanges();
  });

  it.skip('should create', () => {
    expect(component).toBeTruthy();
  });

  it.skip('should fetch open order sorted by priority', () => {
    const orders: OrderSummaryDto[] = [
      {
        id: 1,
        deliveryType: ORDER_DELIVERY_TYPE_OPTION_VALUE,
        statusKey: ORDER_STATUS_OPTION_VALUE,
      },
      {
        id: 2,
        deliveryType: ORDER_DELIVERY_TYPE_OPTION_VALUE,
        statusKey: ORDER_STATUS_OPTION_VALUE,
      },
    ];
    const defaultLazyLoadEvent = component.defaultLazyLoadEvent;

    const response = {
      headers: {
        get: jest.fn(() => 2),
      },
      body: orders,
    };

    spyOn(orderService, 'getOrdersSummaryByCriteria').and.returnValue(of(response));

    component.fetchOrders();

    expect(orderService.getOrdersSummaryByCriteria).toBeCalledWith({
      'statusKey.in': ORDER_STATUS_OPTION_VALUE,
      shipmentType: ShipmentType.CUSTOMER,
      page: defaultLazyLoadEvent.first / defaultLazyLoadEvent.rows,
      size: defaultLazyLoadEvent.rows,
      sort: `${defaultLazyLoadEvent.sortField},${defaultLazyLoadEvent.sortOrder === 1 ? 'ASC' : 'DESC'}`,
    });
    expect(component.totalRecords).toEqual(2);
    expect(component.orders.length).toEqual(orders.length);
    expect(component.orders[0].deliveryTypeDescriptionKey).toEqual(ORDER_DELIVERY_TYPE_DESCRIPTION_KEY);
    expect(component.orders[0].statusDescriptionKey).toEqual(ORDER_STATUS_DESCRIPTION_KEY);
    expect(component.orders[0].statusColor).toEqual(
      component.processProperties[orderStatuses[ORDER_STATUS_OPTION_VALUE]]
    );
  });

  it.skip('should return no-results-found message when there is no order matching the criteria', () => {
    spyOn(orderService, 'getOrdersSummaryByCriteria').and.returnValue(of({ data: { listShipments: null } }));
    spyOn(toaster, 'error');

    component.fetchOrders();

    expect(toaster.error).toBeCalledWith('no-results-found.label');
  });

  it.skip('should navigate to details page', () => {
    const order: OrderSummary = {
      id: 1,
    };

    spyOn(router, 'navigateByUrl');

    component.details(order as OrderSummary);

    expect(router.navigateByUrl).toBeCalledWith('/shipment/1/shipment-details');
  });

  it.skip('should reset filters', () => {
    component.searchString = 'searchString';
    component.placeholder = 'placeholder';
    component.orderSearchGroup.value.statusKey = [{}, {}];
    component.orderSearchGroup.value.order = [...orderFieldsMock];

    component.resetFilters();

    expect(component.searchString).toEqual('');
    expect(component.placeholder).toEqual('');
    expect(component.orderSearchGroup.value.statusKey).toEqual([component.openStatus]);
    expect(component.orderSearchGroup.value.order).toBeNull();
  });

  it.skip('should apply filters', () => {
    const defaultLazyLoadEvent = component.defaultLazyLoadEvent;

    spyOn(orderService, 'getOrdersSummaryByCriteria').and.returnValue(of({ body: [] }));
    spyOn(component, 'fetchOrders').and.callThrough();

    component.orderSearchGroup.value.orderNumber = '1234';
    component.applyFilters();

    expect(component.fetchOrders).toBeCalled();
    expect(orderService.getOrdersSummaryByCriteria).toBeCalledWith({
      'statusKey.in': ORDER_STATUS_OPTION_VALUE,
      shipmentType: ShipmentType.CUSTOMER,
      orderNumber: component.orderSearchGroup.value.orderNumber,
      page: defaultLazyLoadEvent.first / defaultLazyLoadEvent.rows,
      size: defaultLazyLoadEvent.rows,
      sort: `${defaultLazyLoadEvent.sortField},${defaultLazyLoadEvent.sortOrder === 1 ? 'ASC' : 'DESC'}`,
    });
  });

  it.skip('should enable the search button when order (External Order Id and/or Order Number) criteria is selected and all have value', () => {
    const searchString = '123,2345';

    component.orderSearchGroup.value.order = ['externalOrderId', 'orderNumber'];

    component.searchStringChange(searchString);

    expect(component.enableSearchBtn).toBeTruthy();
  });

  it.skip('should successfuly validate manually date entered', () => {
    const createDateManualValue = { currentTarget: { value: '02/18/2022', id: 'createDateFrom' } };

    component.orderSearchGroup.value.createDateTo = new Date();
    component.dateChange(createDateManualValue, 'createDateFrom', 'createDateTo');

    expect(component.orderSearchGroup.get('createDateFrom').errors).toBeFalsy();
    expect(component.orderSearchGroup.get('createDateTo').errors).toBeFalsy();
  });

  it.skip('should invalidate date field with invalid dates', () => {
    const createDateManualValue = { currentTarget: { value: '02/80/2022', id: 'createDateFrom' } };

    component.dateChange(createDateManualValue, 'createDateFrom', 'createDateTo');

    expect(component.orderSearchGroup.get('createDateFrom').hasError('outOfDate')).toBeTruthy();
  });
});
