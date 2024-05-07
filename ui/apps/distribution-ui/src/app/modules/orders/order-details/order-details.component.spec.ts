import { HttpResponse } from '@angular/common/http';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { provideMockStore } from '@ngrx/store/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  CustomerService,
  getAppInitializerMockProvider,
  INTERNAL_TRANSFER_ORDER_PACKING_SLIP,
  INTERNAL_TRANSFER_ORDER_REVIEW_REPORT_PRINT_TYPE,
  InventoryService,
  LookUpDto,
  OrderDto,
  OrderItemInventoryDto,
  OrderService,
  ORDER_PACKING_SLIP,
  ORDER_PRINT_TYPE,
  ORDER_SHIPPING_LABEL,
  PDF_EXTENSION,
  PrintService,
  ReasonDto,
  RuleResponseDto,
  toasterMockProvider,
  TranslateInterpolationPipe,
} from '@rsa/commons';
import { CANCEL_REASON_ID, CLOSE_REASON_ID, OrderProduct } from '@rsa/distribution/core/models/orders.model';
import { SortService } from '@rsa/distribution/core/services/sort.service';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import {
  bloodTypesMock,
  orderProductsMock,
  productAttributesMock,
  productFamiliesMock,
} from '@rsa/distribution/data/mock/orders.mock';
import { OrderWidgetsSidebarComponent } from '@rsa/distribution/shared/components/order-widgets-sidebar/order-widgets-sidebar.component';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { startCase } from 'lodash';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressBarModule } from 'primeng/progressbar';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { CancelOrderModalComponent } from '../cancel-order-modal/cancel-order-modal.component';
import { CloseOrderModalComponent } from '../close-order-modal/close-order-modal.component';
import { OrderDetailsComponent } from './order-details.component';

const ORDER_STATUS_DESCRIPTION_KEY = 'order_status.label';
const ORDER_STATUS_OPTION_VALUE = 'OPEN';
const ORDER_DELIVERY_TYPE_DESCRIPTION_KEY = 'order_delivery_type.label';
const ORDER_DELIVERY_TYPE_OPTION_VALUE = 'order_delivery_type_option_value';
const ORDER_SHIPPING_METHOD_DESCRIPTION_KEY = 'order_shipping_method.label';
const ORDER_SHIPPING_METHOD_OPTION_VALUE = 'order_shipping_method_otpion_value';
const ORDER_ID = 1;
const SHIPPING_CUSTOMER_ID = 1;
const BILLING_CUSTOMER_ID = 2;
const REASON_ID = 1;
const INTERNAL_SHIPMENT_TYPE = 'INTERNAL';

const order: OrderDto = {
  id: ORDER_ID,
  orderNumber: 1,
  externalId: 'string',
  shipmentType: INTERNAL_SHIPMENT_TYPE,
  shippingCustomerId: SHIPPING_CUSTOMER_ID,
  billingCustomerId: BILLING_CUSTOMER_ID,
  locationId: 0,
  deliveryType: 'string',
  priority: 0,
  shippingMethod: 'string',
  productCategoryKey: 'string',
  desireShippingDate: '2022-02-21',
  comments: 'string',
  statusKey: 'string',
  cancelReasonId: 0,
  cancelDate: '2022-02-21',
  cancelEmployeeId: 'string',
  reviewDate: '2022-02-21',
  reviewEmployeeId: 'string',
  downtimeOrder: true,
  downtimeComments: 'string',
  externalOrder: true,
  completeReasonId: 0,
  completeDate: '2022-02-21',
  completeEmployeeId: 'string',
  deleteDate: '2022-02-21T22:05:32.552Z',
  createDate: '2022-02-21T22:05:32.552Z',
  modificationDate: '2022-02-21T22:05:32.552Z',
  orderItems: [
    {
      id: 0,
      orderId: 0,
      productFamily: 'string',
      bloodTypeId: bloodTypesMock[0].id,
      quantity: 0,
      comments: 'string',
      filled: true,
      createDate: '2022-02-21T22:05:32.552Z',
      modificationDate: '2022-02-21T22:05:32.552Z',
      orderItemProductAttributes: [
        {
          id: 0,
          orderItemId: 0,
          productAttributeId: productAttributesMock[0].id,
          productAttributeOptions: [
            {
              id: 0,
              orderItemProductAttributeId: 0,
              attributeOptionValue: 'attrOptionValue',
            },
          ],
        },
      ],
    },
  ],
  orderServiceFees: [
    {
      id: 0,
      orderId: 0,
      serviceFee: 'string',
      quantity: 0,
    },
  ],
};

describe('OrderDetailsComponent', () => {
  let component: OrderDetailsComponent;
  let fixture: ComponentFixture<OrderDetailsComponent>;
  let orderService: OrderService;
  let customerService: CustomerService;
  let router: Router;
  let matDialog: MatDialog;
  let toaster: ToastrService;
  let printService: PrintService;
  let inventoryService: InventoryService;
  let sortService: SortService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [OrderDetailsComponent, OrderWidgetsSidebarComponent],
      schemas: [NO_ERRORS_SCHEMA],
      imports: [
        TreoCardModule,
        MaterialModule,
        TableModule,
        DropdownModule,
        MultiSelectModule,
        ProgressBarModule,
        ButtonModule,
        RippleModule,
        MatTableModule,
        MatPaginatorModule,
        MatProgressBarModule,
        MatSortModule,
        BrowserAnimationsModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...getAppInitializerMockProvider('distribution-app'),
        ...toasterMockProvider,
        TranslateInterpolationPipe,
        { provide: MatDialogRef, useClass: MatDialogRefMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: {
                id: ORDER_ID,
              },
              data: {
                orderDetailsConfigData: [
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
                        descriptionKey: ORDER_SHIPPING_METHOD_DESCRIPTION_KEY,
                        type: 'ORDER_SHIPPING_METHOD',
                        optionValue: ORDER_SHIPPING_METHOD_OPTION_VALUE,
                      },
                    ],
                  },
                  {
                    body: bloodTypesMock,
                  },
                  {
                    body: productAttributesMock,
                  },
                  {
                    body: <ReasonDto[]>[
                      {
                        id: 1,
                        active: true,
                        descriptionKey: 'reasonDescriptionKey',
                        orderNumber: 1,
                        reasonType: CANCEL_REASON_ID,
                      },
                      {
                        id: 2,
                        active: true,
                        descriptionKey: 'reasonDescriptionKey',
                        orderNumber: 2,
                        reasonType: CLOSE_REASON_ID,
                      },
                    ],
                  },
                ],
              },
            },
          },
        },
        provideMockStore(),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<OrderDetailsComponent>(OrderDetailsComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    orderService = TestBed.inject(OrderService);
    customerService = TestBed.inject(CustomerService);
    router = TestBed.inject(Router);
    matDialog = TestBed.inject(MatDialog);
    toaster = TestBed.inject(ToastrService);
    printService = TestBed.inject(PrintService);
    inventoryService = TestBed.inject(InventoryService);
    sortService = TestBed.inject(SortService);

    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  //TODO: Fix it
  it.skip('should fetch order', () => {
    spyOn(orderService, 'getOrderById').and.returnValue(of({ body: order } as HttpResponse<OrderDto>));
    spyOn(orderService, 'getOrderItemInventoryByCriteria').and.returnValue(
      of({ body: [] } as HttpResponse<OrderItemInventoryDto[]>)
    );
    spyOn(customerService, 'getCustomerByCriteria').and.returnValue([
      of({ body: [{ name: 'shippingCustomerName' }, { name: 'billingCustomerName' }] }),
    ]);
    spyOn(sortService, 'sortByDescriptionKey').and.returnValue(productAttributesMock);

    component.bloodTypes = bloodTypesMock;
    component.productAttributes = productAttributesMock;
    component.fetchOrder();

    expect(orderService.getOrderById).toBeCalledWith(ORDER_ID);
    expect(orderService.getOrderItemInventoryByCriteria).toBeCalledWith({ order: order.id, filled: true, size: 1000 });
    expect(sortService.sortByDescriptionKey).toBeCalledTimes(1);
    expect(customerService.getCustomerByCriteria).toBeCalledTimes(1);
    expect(customerService.getCustomerByCriteria).toBeCalledWith({
      'id.in': `${SHIPPING_CUSTOMER_ID},${BILLING_CUSTOMER_ID}`,
    });
  });

  it('should navigate to edit order', () => {
    spyOn(router, 'navigateByUrl');

    component.currentOrder = order;
    component.editOrder();

    expect(router.navigateByUrl).toBeCalledWith(`/orders/${ORDER_ID}/edit`);
  });

  it('should navigate to fill order', () => {
    const nextRoute = `/orders/${ORDER_ID}/fill/1`;

    spyOn(router, 'navigateByUrl');
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications: [{ message: 'sucess message', notificationType: 'success', statusCode: '200' }],
          _links: { next: nextRoute },
        },
      } as HttpResponse<RuleResponseDto>)
    );

    component.currentOrder = order;
    component.loggedUserId = '1';
    component.fillOrder(orderProductsMock[0] as OrderProduct);

    expect(inventoryService.validate).toBeCalledWith({
      ruleName: 'fill-order',
      orderId: order.id,
      orderItemId: 1,
      employeeId: '1',
    });
    expect(router.navigateByUrl).toBeCalledWith(nextRoute, {
      replaceUrl: true,
      state: {
        product: orderProductsMock[0],
        currentOrder: order,
        totalShippedProducts: 0,
        prodIcon: component.getIcon(''),
        orderInfoDescriptions: [],
        billInfoDescriptions: [],
        shippingInfoDescriptions: [],
        feesInfoDescriptions: [],
      },
    });
  });

  it('should not navigate to fill order', () => {
    const notifications = [{ message: 'error message', notificationType: 'error', statusCode: '400' }];

    spyOn(toaster, 'show');
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications,
        },
      } as HttpResponse<RuleResponseDto>)
    );

    component.currentOrder = order;
    component.loggedUserId = '1';
    component.fillOrder(orderProductsMock[0] as OrderProduct);

    expect(toaster.show).toBeCalledWith(
      notifications[0].message,
      startCase(notifications[0].notificationType),
      {},
      notifications[0].notificationType
    );
  });

  it('should navigate back to search order', () => {
    spyOn(router, 'navigateByUrl');

    component.back();

    expect(router.navigateByUrl).toBeCalledWith('/orders/search');
  });

  it('should print', () => {
    spyOn(printService, 'print').and.returnValue(of({ body: { file: {} } }));

    component.currentOrder = order;
    component.printOrder();

    expect(printService.print).toBeCalledWith(PDF_EXTENSION, INTERNAL_TRANSFER_ORDER_REVIEW_REPORT_PRINT_TYPE, {
      referenceId: ORDER_ID,
    });
  });

  it('should print slip for Internal shipmentType', () => {
    spyOn(printService, 'print').and.returnValue(of({ body: { file: {} } }));

    component.currentOrder = order;
    component.printSlip({ id: 1, locationIdTo: 1, locationId: 1 });

    expect(printService.print).toBeCalledWith(PDF_EXTENSION, INTERNAL_TRANSFER_ORDER_PACKING_SLIP, {
      shipmentId: 1,
      shipmentType: INTERNAL_SHIPMENT_TYPE,
    });
  });

  it('should print Label for Internal shipmentType', () => {
    spyOn(printService, 'print').and.returnValue(of({ body: { file: {} } }));

    component.currentOrder = order;
    component.printLabel({ id: 1, locationIdTo: 1, locationId: 1 });

    expect(printService.print).toBeCalledWith(PDF_EXTENSION, ORDER_SHIPPING_LABEL, {
      shipmentId: 1,
      shipmentType: INTERNAL_SHIPMENT_TYPE,
    });
  });

  it('should cancel order', () => {
    const reasons: ReasonDto[] = [
      {
        active: true,
        descriptionKey: 'descriptionKey',
        orderNumber: 1,
        reasonType: 16,
        id: REASON_ID,
      },
    ];

    spyOn(orderService, 'cancelOrder').and.returnValue(of({}));
    spyOn(router, 'navigateByUrl');
    spyOn(toaster, 'success');
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { cancellationReasons: reasons },
      afterClosed: () => of(REASON_ID),
    } as MatDialogRef<CancelOrderModalComponent>);

    component.currentOrder = order;
    component.cancelOrder();

    expect(matDialog.open).toBeCalled();
    expect(router.navigateByUrl).toBeCalledWith('/orders/search');
    expect(orderService.cancelOrder).toBeCalledWith(ORDER_ID, REASON_ID);
    expect(toaster.success).toBeCalledWith('the-order-has-been-cancelled.label');
  });

  it('should close filled order without opening modal and without calling close order from service', () => {
    const reasons: ReasonDto[] = [
      {
        active: true,
        descriptionKey: 'descriptionKey',
        orderNumber: 1,
        reasonType: 11,
        id: REASON_ID,
      },
    ];

    spyOn(orderService, 'closeOrder').and.returnValue(of({}));
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          results: {
            requiredReason: [false],
          },
          notifications: [{ message: 'rul-0092-order-closed.label', notificationType: 'success', statusCode: '200' }],
          _links: { next: 'nextRoute' },
        },
      } as HttpResponse<RuleResponseDto>)
    );
    spyOn(toaster, 'success');
    spyOn(component, 'fetchOrder');
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { closeOrderReasons: reasons },
      afterClosed: () => of(REASON_ID),
    } as MatDialogRef<CloseOrderModalComponent>);

    component.currentOrder = order;
    component.closeOrder();

    expect(matDialog.open).not.toBeCalled();
    expect(inventoryService.validate).toBeCalledTimes(1);
    expect(orderService.closeOrder).toBeCalledTimes(0);
    expect(toaster.success).toBeCalledWith('the-order-has-been-closed.label');
  });

  it('should close unfilled order opening modal and calling orderService', () => {
    const reasons: ReasonDto[] = [
      {
        active: true,
        descriptionKey: 'descriptionKey',
        orderNumber: 1,
        reasonType: 11,
        id: REASON_ID,
      },
    ];

    spyOn(orderService, 'closeOrder').and.returnValue(of({}));
    spyOn(toaster, 'success');
    spyOn(component, 'fetchOrder');
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          results: {
            requiredReason: [true],
          },
          notifications: [
            {
              message: 'rul-0092-order-cannot-close.label',
              notificationType: 'success',
              statusCode: '200',
            },
          ],
          _links: { next: 'nextRoute' },
        },
      } as HttpResponse<RuleResponseDto>)
    );
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { closeOrderReasons: reasons },
      afterClosed: () => of(REASON_ID),
    } as MatDialogRef<CloseOrderModalComponent>);

    component.currentOrder = order;
    component.products = [
      {
        id: 0,
        quantity: 2,
        bloodType: bloodTypesMock[0],
        productFamily: productFamiliesMock[0],
      },
    ];
    component.closeOrder();

    expect(matDialog.open).toBeCalled();
    expect(orderService.closeOrder).toBeCalledWith(ORDER_ID, REASON_ID);
    expect(toaster.success).toBeCalledWith('the-order-has-been-closed.label');
  });

  it('should cancel close order when rule returns error', () => {
    const reasons: ReasonDto[] = [
      {
        active: true,
        descriptionKey: 'descriptionKey',
        orderNumber: 1,
        reasonType: 11,
        id: REASON_ID,
      },
    ];

    spyOn(orderService, 'closeOrder').and.returnValue(of({}));
    spyOn(toaster, 'show');
    spyOn(toaster, 'success');
    spyOn(component, 'fetchOrder');

    const notification = { message: 'rul-0092-order-cannot-close.label', notificationType: 'error', statusCode: '400' };

    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications: [notification],
          _links: { next: 'nextRoute' },
        },
      } as HttpResponse<RuleResponseDto>)
    );
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { closeOrderReasons: reasons },
      afterClosed: () => of(REASON_ID),
    } as MatDialogRef<CloseOrderModalComponent>);

    component.currentOrder = order;
    component.products = [
      {
        id: 0,
        quantity: 2,
        bloodType: bloodTypesMock[0],
        productFamily: productFamiliesMock[0],
      },
    ];

    component.closeOrder();

    expect(matDialog.open).not.toBeCalled();
    expect(orderService.closeOrder).not.toBeCalled();
    expect(toaster.success).not.toBeCalled();
    expect(toaster.show).toBeCalledWith(
      notification.message,
      startCase(notification.notificationType),
      {},
      notification.notificationType
    );
  });
});
