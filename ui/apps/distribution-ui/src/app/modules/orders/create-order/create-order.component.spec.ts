import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialog, MatDialogConfig, MatDialogRef } from '@angular/material/dialog';
import { MatStepperModule } from '@angular/material/stepper';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  CustomerDto,
  FacilityService,
  getAppInitializerMockProvider,
  LookUpDto,
  OrderDto,
  OrderService,
  RsaCommonsModule,
  toasterMockProvider,
} from '@rsa/commons';
import {
  OPEN_OPTION_VALUE,
  OrderFee,
  OrderProduct,
  ORDER_DELIVERY_TYPE,
  ORDER_SHIPPING_CUSTOMER,
} from '@rsa/distribution/core/models/orders.model';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { OrderWidgetsSidebarComponent } from '@rsa/distribution/shared/components/order-widgets-sidebar/order-widgets-sidebar.component';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ConfirmationDialogComponent } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { AddProductModalComponent } from '../add-product-modal/add-product-modal.component';
import { ServiceFeeModalComponent } from '../service-fee-modal/service-fee-modal.component';
import { CreateOrderComponent } from './create-order.component';

describe('CreateOrderComponent', () => {
  let component: CreateOrderComponent;
  let fixture: ComponentFixture<CreateOrderComponent>;
  let toaster: ToastrService;
  let matDialog: MatDialog;
  let orderService: OrderService;
  let facilityService: FacilityService;
  let router: Router;

  const priorities: LookUpDto[] = [
    {
      active: true,
      descriptionKey: 'descriptionKey',
      id: 1,
      optionValue: 'deliveryType',
      type: ORDER_DELIVERY_TYPE,
      orderNumber: 1,
    },
  ];

  const order: OrderDto = {
    billingCustomerId: 1,
    comments: 'comments',
    deliveryType: 'deliveryType',
    desireShippingDate: '2023-07-20T23:02:51',
    downtimeOrder: false,
    externalId: '123456789123456',
    externalOrder: false,
    labelStatus: 'LABELED',
    locationId: 1,
    orderNumber: 1,
    orderServiceFees: [],
    priority: 1,
    productCategoryKey: 'prodCategoryKey',
    shippingCustomerId: 1,
    shippingMethod: 'shippingMethod',
    shipmentType: 'CUSTOMER',
    shippingLocationId: null,
    statusKey: OPEN_OPTION_VALUE,
    orderItems: [],
  };

  const product: OrderProduct = {
    bloodType: {
      productFamily: 'productFamily',
      active: true,
      bloodTypeValue: 'bloodTypeValue',
      descriptionKey: 'descriptionKey',
      orderNumber: 1,
    },
    productFamily: {
      active: true,
      descriptionKey: 'descriptionKey',
      familyCategory: 'familyCategory',
      familyType: 'familyType',
      familyValue: 'familyValue',
    },
    quantity: 1,
    rowIndex: 0,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CreateOrderComponent, OrderWidgetsSidebarComponent],
      imports: [
        RsaCommonsModule,
        TreoCardModule,
        TableModule,
        ButtonModule,
        RippleModule,
        MaterialModule,
        MatAutocompleteModule,
        MatStepperModule,
        MatNativeDateModule,
        FormsModule,
        ReactiveFormsModule,
        HttpClientTestingModule,
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
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<CreateOrderComponent>(CreateOrderComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    toaster = TestBed.inject(ToastrService);
    matDialog = TestBed.inject(MatDialog);
    orderService = TestBed.inject(OrderService);
    facilityService = TestBed.inject(FacilityService);
    router = TestBed.inject(Router);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should filter customer by name', () => {
    const customers: CustomerDto[] = [
      {
        name: 'Eduardo',
        externalId: '123',
        isbtCodeList: [],
        customerAddresses: [],
      },
      {
        name: 'Antonio',
        externalId: '456',
        isbtCodeList: [],
        customerAddresses: [],
      },
      {
        name: 'Zerg',
        externalId: '666',
        isbtCodeList: [],
        customerAddresses: [],
      },
    ];

    component.customers.SHIPPING = customers;
    const filteredCustomers = component.filterCustomers('Z', ORDER_SHIPPING_CUSTOMER);
    filteredCustomers.subscribe(c => {
      expect(c).toHaveLength(1);
      expect(c).toEqual([
        {
          name: 'Zerg',
          externalId: '666',
          isbtCodeList: [],
          customerAddresses: [],
        },
      ]);
    });
  });

  it('should filter customer by external id', () => {
    const customers: CustomerDto[] = [
      {
        name: 'Eduardo',
        externalId: '123',
        isbtCodeList: [],
        customerAddresses: [],
      },
      {
        name: 'Antonio',
        externalId: '456',
        isbtCodeList: [],
        customerAddresses: [],
      },
      {
        name: 'Zerg',
        externalId: '666',
        isbtCodeList: [],
        customerAddresses: [],
      },
    ];
    component.customers.SHIPPING = customers;
    component.orderGroup.value.customerSearchCriteria = 'customer-id';
    const filteredCustomers = component.filterCustomers('666', ORDER_SHIPPING_CUSTOMER);
    filteredCustomers.subscribe(c => {
      expect(c).toHaveLength(1);
      expect(c).toEqual([
        {
          name: 'Zerg',
          externalId: '666',
          isbtCodeList: [],
          customerAddressList: [],
        },
      ]);
    });
  });

  it('should set product selection info', () => {
    const stepperEvent = {
      selectedIndex: 1,
    };

    component.selectedShipCustomer = { name: 'customer', externalId: '1', isbtCodeList: [], customerAddresses: [] };
    component.stepSelectionChange(stepperEvent as StepperSelectionEvent);

    expect(component.productSelectionStep).toBeTruthy();
    expect(component.reviewAndSubmitStep).toBeFalsy();
  });

  it('should fill review and submit', () => {
    const stepperEvent = {
      selectedIndex: 2,
    };

    component.selectedShipCustomer = { name: 'customer', externalId: '1', isbtCodeList: [], customerAddresses: [] };
    component.selectedBillCustomer = { name: 'customer', externalId: '1', isbtCodeList: [], customerAddresses: [] };
    component.stepSelectionChange(stepperEvent as StepperSelectionEvent);

    expect(component.reviewAndSubmitStep).toBeTruthy();
    expect(component.productSelectionStep).toBeFalsy();
  });

  it('should open service fee modal', () => {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '60rem';
    dialogConfig.data = {
      addedFees: [],
      serviceFees: [],
    };

    spyOn(matDialog, 'open').and.returnValue({
      afterClosed: () => of([{ quantity: 1, serviceFee: 'serviceFee' }] as OrderFee[]),
    } as MatDialogRef<ServiceFeeModalComponent>);

    component.serviceFeeClick();

    expect(matDialog.open).toBeCalled();
    expect(component.orderGroup.value.orderServiceFees.length).toEqual(1);
    expect(component.orderGroup.value.orderServiceFees[0].quantity).toEqual(1);
  });

  it('should add a product', () => {
    spyOn(component.productTable, 'reset');

    component.addOrEditProduct(product, false);

    expect(component.products.length).toEqual(1);
    expect(component.products[0].bloodType.productFamily).toEqual('productFamily');
    expect(component.productTable.reset).toBeCalled();
    expect(component.productCountControl.value).toEqual(1);
  });

  it('should edit a product', () => {
    spyOn(component.productTable, 'reset');

    component.products.push(product);
    product.bloodType.productFamily = 'productFamilyEdited';

    component.addOrEditProduct(product, true);

    expect(component.products.length).toEqual(1);
    expect(component.products[0].bloodType.productFamily).toEqual('productFamilyEdited');
    expect(component.productTable.reset).toBeCalled();
    expect(component.productCountControl.value).toEqual(1);
  });

  it('should open add a product dialog', () => {
    spyOn(matDialog, 'open').and.returnValue({
      afterClosed: () =>
        of({
          productFamily: {
            active: true,
            descriptionKey: 'descriptionKey',
            familyCategory: 'familyCategory',
            familyType: 'familyType',
            familyValue: 'familyValue',
          },
          productAttributes: [],
          bloodTypeAndQuantity: [
            {
              bloodType: {
                active: true,
                bloodTypeValue: 'bloodTypeValue',
                descriptionKey: 'descriptionKey',
                orderNumber: 1,
                productFamily: 'productFamily',
              },
              quantity: 1,
            },
            {
              bloodType: {
                active: true,
                bloodTypeValue: 'bloodTypeValue2',
                descriptionKey: 'descriptionKey2',
                orderNumber: 2,
                productFamily: 'productFamily',
              },
              quantity: 3,
            },
          ],
          productComment: '',
          quantity: 1,
          bloodType: {
            active: true,
            bloodTypeValue: 'bloodTypeValue',
            descriptionKey: 'descriptionKey',
            orderNumber: 1,
            productFamily: 'productFamily',
          },
        } as OrderProduct),
      beforeClosed: () => of(null),
    } as MatDialogRef<AddProductModalComponent>);

    component.addProduct();

    expect(matDialog.open).toBeCalled();
    expect(component.products.length).toEqual(2);
  });

  it('should delete product', () => {
    spyOn(component.productTable, 'reset');

    component.products.push(product);
    component.delete(0);

    expect(component.products.length).toEqual(0);
    expect(component.productTable.reset).toBeCalled();
    expect(component.productCountControl.value).toEqual(0);
  });

  it('should open cancel order dialog', () => {
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { dialogText: '', dialogTitle: '', acceptBtnTittle: '' },
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmationDialogComponent>);
    spyOn(router, 'navigateByUrl');

    component.cancelOrder();

    expect(matDialog.open).toBeCalled();
    expect(router.navigateByUrl).toBeCalled();
  });

  it('should submit new order', () => {
    spyOn(facilityService, 'getFacilityId').and.returnValue(1);
    spyOn(orderService, 'createOrder').and.returnValue(of({ id: 1 } as OrderDto));
    spyOn(toaster, 'success');
    spyOn(router, 'navigateByUrl');

    component.priorities = priorities;
    component.selectedShipCustomer = {
      id: 1,
      name: 'customer',
      externalId: '1',
      isbtCodeList: [],
      customerAddresses: [],
    };
    component.selectedBillCustomer = {
      id: 1,
      name: 'customer',
      externalId: '1',
      isbtCodeList: [],
      customerAddresses: [],
    };
    component.orderGroup.patchValue(order);

    component.onSubmit();

    expect(orderService.createOrder).toBeCalledWith({
      ...component.orderGroup.value,
      ...order,
    });
    expect(toaster.success).toBeCalledWith('the-order-has-been-submitted.label');
    expect(router.navigateByUrl).toBeCalledWith(`/orders/1/details`);
  });

  //TODO: Fix this
  it.skip('should edit order', () => {
    spyOn(facilityService, 'getFacilityId').and.returnValue(1);
    spyOn(orderService, 'editOrder').and.returnValue(of({ id: 1 } as OrderDto));
    spyOn(toaster, 'success');
    spyOn(router, 'navigateByUrl');

    component.priorities = priorities;
    component.selectedShipCustomer = {
      id: 1,
      name: 'customer',
      externalId: '1',
      isbtCodeList: [],
      customerAddresses: [],
    };
    component.selectedBillCustomer = {
      id: 1,
      name: 'customer',
      externalId: '1',
      isbtCodeList: [],
      customerAddresses: [],
    };
    component.orderGroup.patchValue(order);

    component.onSubmit();

    expect(orderService.editOrder).toBeCalledWith({
      ...component.orderGroup.value,
      ...order,
    });
    expect(toaster.success).toBeCalledWith('the-order-has-been-submitted.label');
    expect(router.navigateByUrl).toBeCalledWith(`/orders/1/details`);
  });
});
