import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  AutoFocusIfDirective,
  BarcodeService,
  ControlErrorsDirective,
  FileService,
  getAppInitializerMockProvider,
  InventoryService,
  LabelStatus,
  LockService,
  OrderService,
  ProductsService,
  toasterMockProvider,
  TranslateInterpolationPipe,
  ValidationPipe,
  WidgetComponent,
} from '@rsa/commons';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
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

describe('ProductSelectionComponent', () => {
  let component: ProductSelectionComponent;
  let fixture: ComponentFixture<ProductSelectionComponent>;
  let router: Router;
  let toaster: ToastrService;
  let matDialog: MatDialog;
  let lockService: LockService;
  let inventoryService: InventoryService;
  let barcodeService: BarcodeService;
  let productService: ProductsService;
  let orderService: OrderService;
  let fileService: FileService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ProductSelectionComponent,

        WidgetComponent,

        AutoFocusIfDirective,
        ControlErrorsDirective,
        ValidationPipe,
        ProductSelectionComponent,
      ],
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
        MatSortModule,
        MatProgressBarModule,
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
        TranslateInterpolationPipe,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<ProductSelectionComponent>(ProductSelectionComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    router = TestBed.inject(Router);
    toaster = TestBed.inject(ToastrService);
    matDialog = TestBed.inject(MatDialog);
    lockService = TestBed.inject(LockService);
    inventoryService = TestBed.inject(InventoryService);
    orderService = TestBed.inject(OrderService);
    fileService = TestBed.inject(FileService);
    barcodeService = TestBed.inject(BarcodeService);
    productService = TestBed.inject(ProductsService);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should stop product selection workflow if shouldProceedWithProductSelection function returns false', () => {
    component.currentOrder = mockOrder('UNLABELED');

    const UNIT_NUMBER = 'W038196752085';

    spyOn(inventoryService, 'validate').and.returnValue(of(mockRule0115Return(['PROD1', 'PROD2'])));

    spyOn(productService, 'getProductsByProductCodes').and.returnValue(
      of(mockProductsByProductCodes(['PROD1', 'PROD2']))
    );

    spyOn(matDialog, 'open');
    spyOn(barcodeService, 'getBarcodeTranslation').and.returnValue(
      of({ body: { barcodeTranslation: { ['unitNumber']: UNIT_NUMBER } } })
    );

    component.shouldProceedWithProductSelection = () => false;

    component.productGroup.get('unitNumber').setValue(UNIT_NUMBER);
    component.onEnterKeyOrTab('unitNumber');

    expect(inventoryService.validate).toBeCalledTimes(0);
    expect(productService.getProductsByProductCodes).toBeCalledTimes(0);
  });

  it('should open product selection modal for unlabeled internal transfers', () => {
    component.currentOrder = mockOrder('UNLABELED');

    const UNIT_NUMBER = 'W038196752085';

    spyOn(inventoryService, 'validate').and.returnValue(of(mockRule0115Return(['PROD1', 'PROD2'])));

    spyOn(productService, 'getProductsByProductCodes').and.returnValue(
      of(mockProductsByProductCodes(['PROD1', 'PROD2']))
    );

    spyOn(matDialog, 'open');
    spyOn(barcodeService, 'getBarcodeTranslation').and.returnValue(
      of({ body: { barcodeTranslation: { ['unitNumber']: UNIT_NUMBER } } })
    );

    component.productGroup.get('unitNumber').setValue(UNIT_NUMBER);
    component.onEnterKeyOrTab('unitNumber');
    expect(inventoryService.validate).toBeCalledWith({
      facilityId: null,
      productCategoryKey: 'product-category.key',
      ruleName: 'rul-0115-get-products-by-unit-number-and-status',
      inventoryIDList: [],
      unitNumber: UNIT_NUMBER,
    });
    expect(productService.getProductsByProductCodes).toBeCalledTimes(1);
  });

  it('should not open product selection modal for unlabeled internal transfers when unit number contains only one available inventory', () => {
    const orderItemInventory = { filled: false, inventoryId: 1, order: 1, orderItem: 1 };
    component.currentOrder = mockOrder('UNLABELED');

    const UNIT_NUMBER = 'W038196752085';

    spyOn(inventoryService, 'validate').and.returnValue(of(mockRule0115Return(['PROD1'])));

    spyOn(productService, 'getProductsByProductCodes').and.returnValue(of(mockProductsByProductCodes(['PROD1'])));

    spyOn(matDialog, 'open');
    spyOn(barcodeService, 'getBarcodeTranslation').and.returnValue(
      of({ body: { barcodeTranslation: { ['unitNumber']: UNIT_NUMBER } } })
    );

    component.productGroup.get('unitNumber').setValue(UNIT_NUMBER);
    component.onEnterKeyOrTab('unitNumber');
    expect(inventoryService.validate).toBeCalledWith({
      facilityId: null,
      ruleName: 'rul-0115-get-products-by-unit-number-and-status',
      productCategoryKey: 'product-category.key',
      inventoryIDList: [],
      unitNumber: UNIT_NUMBER,
    });
    expect(productService.getProductsByProductCodes).toBeCalledTimes(0);
  });

  it('should translate the scanned unit number', () => {
    const unitNumber = 'W999914003459';

    component.currentOrder = mockOrder('LABELED');
    component.ngAfterViewInit();

    spyOn(barcodeService, 'getBarcodeTranslation').and.returnValue(
      of({ body: { barcodeTranslation: { unitNumber: unitNumber } } })
    );
    component.productGroup.get('unitNumber').setValue(unitNumber);
    component.onEnterKeyOrTab('unitNumber');
    expect(barcodeService.getBarcodeTranslation).toBeCalledWith(unitNumber);
  });

  const mockOrder = (labelStatus: LabelStatus) => ({
    billingCustomerId: 0,
    deliveryType: '',
    desireShippingDate: '',
    downtimeOrder: false,
    externalId: '',
    externalOrder: false,
    locationId: 0,
    orderNumber: 0,
    orderServiceFees: [],
    priority: 0,
    productCategoryKey: 'product-category.key',
    shippingCustomerId: 0,
    shippingMethod: '',
    statusKey: '',
    id: 0,
    shipmentType: 'INTERNAL_TRANSFER',
    labelStatus: labelStatus,
  });

  const mockRule0115Return = (productCodes: string[]) => ({
    body: {
      ruleCode: 'OK',
      results: {
        notificationStatus: [200],
        inventories: [productCodes.map(pc => ({ productCode: pc }))],
        notificationType: ['success'],
      },
      notifications: [],
      _links: {},
    },
  });

  const mockProductsByProductCodes = (productCodes: string[]) => ({
    body: {
      products: productCodes.map(pc => ({
        productCode: pc,
        properties: { icon: pc },
      })),
    },
  });
});
