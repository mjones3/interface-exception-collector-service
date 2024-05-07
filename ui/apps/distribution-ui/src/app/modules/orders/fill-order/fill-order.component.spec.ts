import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormArray } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  AutoFocusIfDirective,
  BarcodeService,
  ControlErrorsDirective,
  DocumentDto,
  FileService,
  getAppInitializerMockProvider,
  InventoryService,
  LabelStatus,
  LockService,
  OrderService,
  ProcessHeaderComponent,
  ProductsService,
  toasterMockProvider,
  TranslateInterpolationPipe,
  UploadDocumentComponent,
  ValidationPipe,
  WidgetComponent,
} from '@rsa/commons';
import { ORDER_ITEM_LOCK_TYPE } from '@rsa/distribution/core/models/orders.model';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { OrderWidgetsSidebarComponent } from '@rsa/distribution/shared/components/order-widgets-sidebar/order-widgets-sidebar.component';
import { ProductSelectionComponent } from '@rsa/distribution/shared/components/product-selection/product-selection.component';
import { MaterialModule } from '@rsa/material';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ConfirmationDialogComponent } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { DescriptionCardComponent } from 'libs/commons/src/lib/components/information-card/description-card.component';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressBarModule } from 'primeng/progressbar';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { FillOrderComponent } from './fill-order.component';

const orderProduct = {
  id: 1,
  bloodType: { active: true, bloodTypeValue: '', descriptionKey: '', orderNumber: 1, productFamily: '' },
  productFamily: { active: true, descriptionKey: '', familyCategory: '', familyType: '', familyValue: '' },
  quantity: 2,
};

describe('FillOrderComponent', () => {
  let component: FillOrderComponent;
  let fixture: ComponentFixture<FillOrderComponent>;
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
        FillOrderComponent,
        ProcessHeaderComponent,
        DescriptionCardComponent,
        UploadDocumentComponent,
        WidgetComponent,
        OrderWidgetsSidebarComponent,
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
        {
          provide: ActivatedRoute,
          useValue: {
            paramMap: of({}),
            snapshot: {
              params: { id: '1', productId: orderProduct.id },
              data: { subTitle: '' },
            },
          },
        },
        TranslateInterpolationPipe,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<FillOrderComponent>(FillOrderComponent);
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

  it('should navigate to order details', () => {
    spyOn(router, 'navigateByUrl');

    component.back();

    expect(router.navigateByUrl).toBeCalledWith(`/orders/${component.orderId}/details`);
  });

  it('should display cancel confirmation dialog and unlock the order item', () => {
    spyOn(lockService, 'unlock').and.returnValue(of());
    spyOn(orderService, 'deleteNotFilledInventories').and.returnValue(of());
    spyOn(router, 'navigateByUrl');
    spyOn(toaster, 'success');
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: {},
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmationDialogComponent>);

    component.products = [
      {
        productCode: 'E1234V00',
        unitNumber: 'W121212121212',
        unlicensed: false,
        orderItemInventory: { id: 1, orderItem: 1, order: 1, inventoryId: 1, filled: false },
      },
    ];
    component.orderProduct = orderProduct;
    component.cancel();

    expect(matDialog.open).toBeCalled();
    expect(router.navigateByUrl).toBeCalledWith(`/orders/${component.orderId}/details`);
    expect(lockService.unlock).toBeCalledWith(ORDER_ITEM_LOCK_TYPE, component.orderProduct.id);
    expect(orderService.deleteNotFilledInventories).toBeCalledWith(component.orderProduct.id);
    expect(toaster.success).toBeCalledWith('cancel-fill-order-success.label');
  });

  it('should display remove confirmation dialog and remove the product', () => {
    spyOn(orderService, 'removeOrderItemInventory').and.returnValue(of({}));
    spyOn(toaster, 'success');
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: {},
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmationDialogComponent>);

    component.products = [
      {
        orderItemInventory: { id: 1, orderItem: 1, order: 1, inventoryId: 1, filled: true },
        productCode: 'E1234V00',
        unitNumber: 'W121212121212',
        unlicensed: false,
      },
    ];
    component.removeProd(0);

    expect(matDialog.open).toBeCalled();
    expect(orderService.removeOrderItemInventory).toBeCalledWith(1);
    expect(component.products.length).toEqual(0);
    expect(toaster.success).toBeCalledWith('remove-product-success.label');
  });

  it('should display remove all confirmation dialog and remove all the products', () => {
    spyOn(orderService, 'removeAllOrderItemInventory').and.returnValue(of({}));
    spyOn(toaster, 'success');
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: {},
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmationDialogComponent>);

    component.products = [
      {
        orderItemInventory: { id: 1, orderItem: 1, order: 1, inventoryId: 1, filled: true },
        productCode: 'E1234V00',
        unitNumber: 'W121212121212',
        unlicensed: false,
      },
      {
        orderItemInventory: { id: 2, orderItem: 1, order: 1, inventoryId: 1, filled: true },
        productCode: 'E1235V00',
        unitNumber: 'W121212121212',
        unlicensed: true,
      },
    ];
    component.orderProduct = orderProduct;
    component.removeAll();

    expect(matDialog.open).toBeCalled();
    expect(component.products.length).toEqual(0);
  });

  it('should save fill order without files', () => {
    const orderItemInventory = { filled: false, inventoryId: 1, order: 1, orderItem: 1 };

    spyOn(orderService, 'saveFillOrder').and.returnValue(of({}));
    spyOn(router, 'navigateByUrl');

    component.orderProduct = orderProduct;
    component.products = [
      {
        orderItemInventory: orderItemInventory,
        productCode: 'E1234V00',
        unitNumber: 'W123456789123',
        unlicensed: false,
      },
    ];
    component.save();

    expect(orderService.saveFillOrder).toBeCalledWith(orderProduct.id, {
      orderItemInventories: component.products.map(prod => prod.orderItemInventory),
      orderItemAttachments: [],
    });
    expect(router.navigateByUrl).toBeCalledWith('/orders/1/details');
  });

  //TODO: Fix
  it.skip('should save fill order with files', () => {
    const orderItemInventory = { filled: false, inventoryId: 1, order: 1, orderItem: 1 };
    const files = [{ name: 'fileTest.pdf' }];
    const documents: DocumentDto[] = [
      {
        id: 1,
        filename: files[0].name,
        referenceId: orderProduct.id,
        createDate: new Date().toISOString(),
        modificationDate: new Date().toISOString(),
      },
    ];

    spyOn(fileService, 'uploadDocument').and.returnValue(of({ body: documents }));
    spyOn(orderService, 'saveFillOrder').and.returnValue(of({}));
    spyOn(router, 'navigateByUrl');

    component.uploadDocumentFiles = files as File[];
    component.uploadFormData = new FormArray([]);
    component.orderProduct = orderProduct;
    component.products = [
      {
        orderItemInventory: orderItemInventory,
        productCode: 'E1234V00',
        unitNumber: 'W123456789123',
        unlicensed: true,
      },
    ];
    component.save();

    expect(orderService.saveFillOrder).toBeCalledWith(orderProduct.id, {
      orderItemInventories: component.products.map(prod => prod.orderItemInventory),
      orderItemAttachments: [
        {
          orderItemId: documents[0].referenceId,
          documentId: documents[0].id,
          description: documents[0].filename,
          createDate: documents[0].createDate,
          modificationDate: documents[0].modificationDate,
        },
      ],
    });
    expect(router.navigateByUrl).toBeCalledWith('/orders/1/details');
  });
});
