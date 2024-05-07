import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import {
  AutoFocusIfDirective,
  BarcodeService, ControlErrorComponent,
  ControlErrorsDirective,
  CustomerDto,
  CustomerService,
  ExternalTransferDto,
  FacilityService, getAppInitializerMockProvider,
  InventoryService,
  OrderDto,
  OrderService,
  ProcessHeaderComponent,
  RuleResponseDto,
  ShipmentService, toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { ADD_PRODUCT_TO_BATCH_VALIDATION_RULE } from '@rsa/distribution/core/models/external-transfers.model';
import { availableProductsMock } from '@rsa/distribution/data/mock/external-transfers.mock';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ConfirmationDialogComponent } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { DescriptionCardComponent } from 'libs/commons/src/lib/components/information-card/description-card.component';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of, throwError } from 'rxjs';
import { AddProductsModalComponent } from '../add-products-modal/add-products-modal.component';
import { ExternalTransfersComponent } from './external-transfers.component';

const form = {
  customerSearchCriteria: 'name',
  transferToCustomer: { name: 'customer', id: 1 },
  transferDate: new Date(),
};

describe('ExternalTransfersComponent', () => {
  let component: ExternalTransfersComponent;
  let fixture: ComponentFixture<ExternalTransfersComponent>;
  let orderService: OrderService;
  let customerService: CustomerService;
  let inventoryService: InventoryService;
  let shipmentService: ShipmentService;
  let facilityService: FacilityService;
  let translateService: TranslateService;
  let barcodeService: BarcodeService;
  let toaster: ToastrService;
  let matDialog: MatDialog;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ExternalTransfersComponent,
        ProcessHeaderComponent,
        DescriptionCardComponent,
        ControlErrorsDirective,
        ControlErrorComponent,
        AutoFocusIfDirective,
        ValidationPipe,
      ],
      imports: [
        TreoCardModule,
        TableModule,
        ButtonModule,
        RippleModule,
        MaterialModule,
        FormsModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        HttpClientTestingModule,
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
        ValidationPipe,
        { provide: MatDialogRef, useClass: MatDialogRefMock },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<ExternalTransfersComponent>(ExternalTransfersComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    orderService = TestBed.inject(OrderService);
    customerService = TestBed.inject(CustomerService);
    inventoryService = TestBed.inject(InventoryService);
    shipmentService = TestBed.inject(ShipmentService);
    facilityService = TestBed.inject(FacilityService);
    translateService = TestBed.inject(TranslateService);
    barcodeService = TestBed.inject(BarcodeService);
    toaster = TestBed.inject(ToastrService);
    matDialog = TestBed.inject(MatDialog);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display cancel confirmation dialog and navigate back to home', () => {
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { dialogText: 'dialog text', dialogTitle: 'dialog title', acceptBtnTittle: 'accept' },
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmationDialogComponent>);
    spyOn(router, 'navigateByUrl');

    component.cancel();

    expect(matDialog.open).toBeCalled();
    expect(router.navigateByUrl).toBeCalledWith('/home');
  });

  it('should navigate to step 2', () => {
    spyOn(component.stepper, 'next').and.callThrough();

    component.transferInfoGroup.patchValue(form);
    component.step1NextClick();

    expect(component.productSelectionStep).toBeTruthy();
  });

  it('should validate external order id and navigate to step 2', () => {
    const externalTransferOrderId = 1;

    spyOn(orderService, 'getOrderByCriteria').and.returnValue(of({ body: [{}] } as HttpResponse<OrderDto[]>));

    component.transferInfoGroup.patchValue({ ...form, externalTransferOrderId });
    component.step1NextClick();

    expect(orderService.getOrderByCriteria).toBeCalledWith({
      'externalId.equals': externalTransferOrderId,
    });
    expect(component.transferInfoGroup.get('externalTransferOrderId').errors).toBeNull();
  });

  it('should validate external order id and show error message', () => {
    const externalTransferOrderId = 1;
    const errorMessage = 'Transfer order id doesn\'t exist';

    spyOn(translateService, 'instant').and.returnValue(errorMessage);
    spyOn(orderService, 'getOrderByCriteria').and.returnValue(of({ body: [] } as HttpResponse<OrderDto[]>));
    spyOn(toaster, 'show');

    component.transferInfoGroup.patchValue({ ...form, externalTransferOrderId });
    component.step1NextClick();

    expect(toaster.show).toBeCalledWith(errorMessage, 'Error', {}, 'error');
    expect(component.transferInfoGroup.get('externalTransferOrderId').value).toEqual('');
  });

  it('should display transfer date change confirmation dialog and clear the products', () => {
    const event = {
      value: new Date(),
    } as MatDatepickerInputEvent<Date>;

    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { dialogText: 'dialog text', dialogTitle: 'dialog title', acceptBtnTittle: 'accept' },
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmationDialogComponent>);

    component.products = availableProductsMock;
    component.transferDateChanged(event);

    expect(component.products.length).toEqual(0);
  });

  it('should display error message for not matching customer', () => {
    spyOn(customerService, 'getCustomerByCriteria').and.returnValue(of({ body: [] } as HttpResponse<CustomerDto[]>));

    component.transferInfoGroup.patchValue({ customerSearchCriteria: 'name' });

    const filteredCustomers = component.filterCustomers('lu');
    filteredCustomers.subscribe(customers => {
      expect(customers).toHaveLength(0);

      expect(component.transferInfoGroup.get('transferToCustomer').errors).toBeDefined();
      expect(component.transferInfoGroup.get('transferToCustomer').hasError('noMatchingCustomer')).toBeTruthy();
    });

    expect(customerService.getCustomerByCriteria).toBeCalledWith({ 'name.contains': 'lu', active: 'true' });
  });

  it('should display add products dialog and update products', () => {
    const unitNumber = 'W123456789098';

    spyOn(barcodeService, 'getBarcodeTranslation').and.returnValue(throwError({}));
    spyOn(inventoryService, 'validate').and.returnValue(
      of({ body: { results: { externalTransferItems: availableProductsMock }, ruleCode: 'OK' } } as HttpResponse<
        RuleResponseDto
      >)
    );
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { unitNumber, availableProducts: availableProductsMock },
      afterClosed: () => of(availableProductsMock),
    } as MatDialogRef<AddProductsModalComponent>);

    component.transferInfoGroup.patchValue(form);
    component.unitNumberControl.patchValue(unitNumber);
    component.onUnitNumberKeyOrTab();

    expect(inventoryService.validate).toBeCalledWith({
      ruleName: ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
      unitNumber: unitNumber,
      inventoryIDList: [],
      transferDate: moment(form.transferDate).format('YYYY-MM-DD'),
    });
    expect(matDialog.open).toBeCalled();
    expect(component.products).toHaveLength(availableProductsMock.length);
    expect(component.unitNumberControl.value).toEqual('');
  });

  it('should complete external transfer', () => {
    const successMessage = 'Products successfully transferred';
    const locationId = 1;

    spyOn(facilityService, 'getFacilityId').and.returnValue(locationId);
    spyOn(shipmentService, 'createExternalTransfer').and.returnValue(of({} as ExternalTransferDto));
    spyOn(translateService, 'instant').and.returnValue(successMessage);
    spyOn(toaster, 'show');

    component.products = availableProductsMock;
    component.transferInfoGroup.patchValue(form);
    component.onComplete();

    expect(shipmentService.createExternalTransfer).toBeCalledWith({
      externalOrderId: '',
      locationId: locationId,
      customerId: form.transferToCustomer.id,
      transferDate: moment(form.transferDate).format('YYYY-MM-DD'),
      externalTransferItems: availableProductsMock,
    });
    expect(toaster.show).toBeCalledWith(successMessage, 'Success', {}, 'success');
  });

  it('should remove product', () => {
    component.products = [...availableProductsMock];
    component.remove(availableProductsMock[0]);

    expect(component.products.length).toEqual(availableProductsMock.length - 1);
  });

  it('should remove all products', () => {
    const successMessage = 'Products have been removed';

    spyOn(translateService, 'instant').and.returnValue(successMessage);
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { dialogText: 'dialog text', dialogTitle: 'dialog title', acceptBtnTittle: 'accept' },
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmationDialogComponent>);
    spyOn(toaster, 'show');

    component.products = availableProductsMock;
    component.removeAll();

    expect(matDialog.open).toBeCalled();
    expect(component.products).toHaveLength(0);
    expect(toaster.show).toBeCalledWith(successMessage, 'Success', {}, 'success');
  });
});
