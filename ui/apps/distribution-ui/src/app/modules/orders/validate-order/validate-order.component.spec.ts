import { NO_ERRORS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  BarcodeService,
  CustomerAddressDto,
  getAppInitializerMockProvider,
  InventoryService,
  LookUpDto,
  NotificationDto,
  OrderDto,
  OrderItemInventoryDto,
  ShipmentService,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import {
  ORDER_DELIVERY_TYPE,
  ORDER_PRODUCT_CATEGORY,
  ORDER_SHIPPING_CUSTOMER,
  ORDER_SHIPPING_METHOD,
  ORDER_STATUS,
} from '@rsa/distribution/core/models/orders.model';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { bloodTypesMock, productAttributesMock } from '@rsa/distribution/data/mock/orders.mock';
import { OrderWidgetsSidebarModule } from '@rsa/distribution/shared/components/order-widgets-sidebar/order-widgets-sidebar.module';
import { ProductSelectionComponent } from '@rsa/distribution/shared/components/product-selection/product-selection.component';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressBarModule } from 'primeng/progressbar';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { ValidateOrderComponent } from './validate-order.component';

const ORDER_STATUS_DESCRIPTION_KEY = 'order_status.label';
const ORDER_STATUS_OPTION_VALUE = 'OPEN';
const ORDER_DELIVERY_TYPE_DESCRIPTION_KEY = 'order_delivery_type.label';
const ORDER_DELIVERY_TYPE_OPTION_VALUE = 'order_delivery_type_option_value';
const ORDER_SHIPPING_METHOD_DESCRIPTION_KEY = 'order_shipping_method.label';
const ORDER_SHIPPING_METHOD_OPTION_VALUE = 'order_shipping_method_otpion_value';
const ORDER_PRODUCT_LABEL_DESCRIPTION_KEY = 'order_product_label.label';
const ORDER_PRODUCT_LABEL_OPTION_VALUE = 'order_product_label_option_value';
const ORDER_ID = 1;
const SHIPPING_CUSTOMER_ID = 1;
const BILLING_CUSTOMER_ID = 2;

const order: OrderDto = {
  id: ORDER_ID,
  orderNumber: 1,
  externalId: 'string',
  shippingCustomerId: SHIPPING_CUSTOMER_ID,
  billingCustomerId: BILLING_CUSTOMER_ID,
  locationId: 0,
  deliveryType: 'string',
  priority: 0,
  shippingMethod: 'string',
  productCategoryKey: ORDER_PRODUCT_LABEL_OPTION_VALUE,
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

describe('ValidateOrderComponent', () => {
  let component: ValidateOrderComponent;
  let fixture: ComponentFixture<ValidateOrderComponent>;
  let router: Router;
  let toaster: ToastrService;
  let inventoryService: InventoryService;
  let shipmentService: ShipmentService;
  let barcodeService: BarcodeService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ValidateOrderComponent],
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
        OrderWidgetsSidebarModule,
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
        { provide: MatDialogRef, useClass: MatDialogRefMock },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              params: {
                id: ORDER_ID,
              },
              data: {
                validateData: {
                  lookups: <LookUpDto[]>[
                    {
                      id: 1,
                      active: true,
                      descriptionKey: ORDER_STATUS_DESCRIPTION_KEY,
                      type: ORDER_STATUS,
                      optionValue: ORDER_STATUS_OPTION_VALUE,
                    },
                    {
                      id: 2,
                      active: true,
                      descriptionKey: ORDER_DELIVERY_TYPE_DESCRIPTION_KEY,
                      type: ORDER_DELIVERY_TYPE,
                      optionValue: ORDER_DELIVERY_TYPE_OPTION_VALUE,
                    },
                    {
                      id: 3,
                      active: true,
                      descriptionKey: ORDER_SHIPPING_METHOD_DESCRIPTION_KEY,
                      type: ORDER_SHIPPING_METHOD,
                      optionValue: ORDER_SHIPPING_METHOD_OPTION_VALUE,
                    },
                    {
                      id: 3,
                      active: true,
                      descriptionKey: ORDER_PRODUCT_LABEL_DESCRIPTION_KEY,
                      type: ORDER_PRODUCT_CATEGORY,
                      optionValue: ORDER_PRODUCT_LABEL_OPTION_VALUE,
                    },
                  ],
                  shippingCustomer: {
                    name: 'shippingCustomerName',
                    customerAddresses: <CustomerAddressDto[]>[
                      {
                        customerId: SHIPPING_CUSTOMER_ID,
                        addressType: ORDER_SHIPPING_CUSTOMER,
                        city: 'LP',
                        contactName: 'Jim Raynor',
                        state: 'LP',
                        postalCode: 'LP',
                        country: 'BO',
                        countryCode: 'BO',
                        district: 'LP',
                        addressLine1: 'koprulu sector',
                        addressLine2: '',
                      },
                    ],
                  },
                  billingCustomer: { name: 'billingCustomerName' },
                  products: <OrderItemInventoryDto[]>[
                    {
                      id: 74,
                      inventoryId: 20000001526305,
                      productCode: 'E0713V00',
                      unitNumber: 'W038112863614',
                      price: null,
                      filled: true,
                    },
                  ],
                  order: <OrderDto>order,
                },
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<ValidateOrderComponent>(ValidateOrderComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    router = TestBed.inject(Router);
    toaster = TestBed.inject(ToastrService);
    inventoryService = TestBed.inject(InventoryService);
    shipmentService = TestBed.inject(ShipmentService);
    barcodeService = TestBed.inject(BarcodeService);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to back to order details', () => {
    spyOn(router, 'navigateByUrl');

    component.back();

    expect(router.navigateByUrl).toBeCalledWith(`/orders/${ORDER_ID}/details`);
  });

  it('should complete', () => {
    spyOn(shipmentService, 'createShipment').and.returnValue(of({ body: 'Shipment created' }));
    spyOn(router, 'navigateByUrl');
    spyOn(toaster, 'success');
    component.completeOrder();
    expect(router.navigateByUrl).toBeCalledWith(`/orders/${ORDER_ID}/details`);
    expect(shipmentService.createShipment).toBeCalled();
    expect(toaster.success).toBeCalledWith('shipment-created-successfully.label');
  });

  it('should validate the order', () => {
    const validateResponse = {
      notifications: <NotificationDto[]>[
        {
          message: 'Affirmative.',
          notificationType: 'success',
          statusCode: '200',
        },
      ],
    };
    const productCode = 'E1234V00';
    const unitNumber = 'W123456789098';

    spyOn(barcodeService, 'getBarcodeTranslation').and.returnValue(
      of({ body: { barcodeTranslation: { productCode } } })
    );
    spyOn(inventoryService, 'validate').and.returnValue(of({ body: validateResponse }));
    spyOn(toaster, 'show');

    component.products = [
      {
        productCode,
        unitNumber,
        filled: true,
        inventoryId: 1,
        order: 1,
        orderItem: 1,
      },
    ];

    component.unitNumberProductCodeSelected(unitNumber, productCode);

    expect(inventoryService.validate).toBeCalled();
    expect(component.products[0].validated).toBeTruthy();
  });
});
