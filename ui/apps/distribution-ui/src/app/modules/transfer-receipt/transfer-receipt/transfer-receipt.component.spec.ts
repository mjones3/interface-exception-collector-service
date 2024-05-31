import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_MOMENT_DATE_FORMATS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  ConfirmDialogComponent,
  createTestContextAndIcons,
  FacilityService,
  getAppIconsTokenMockProvider,
  getAppInitializerMockProvider,
  InventoryService,
  LookUpDto,
  OrderDto,
  OrderService,
  RsaCommonsModule,
  RulesService,
  toasterMockProvider,
  TransferReceiptService,
  ValidationPipe,
} from '@rsa/commons';
import { IMPORTS_BLOOD_TYPES } from '@rsa/distribution/core/models/imports.models';
import { ORDER_PRODUCT_CATEGORY_REFRIGERATED } from '@rsa/distribution/core/models/orders.model';
import { TransitTimeComponent } from '@rsa/distribution/shared/components/transit-time/transit-time.component';
import { MatDialogRefMock } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { allDistributionIcons } from '../../../../icons';
import { TemperatureInputComponent } from '../temperature-input/temperature-input.component';
import { TransferReceiptProductSelectionComponent } from '../transfer-receipt-product-selection/transfer-receipt-product-selection.component';
import { TransferReceiptComponent } from './transfer-receipt.component';

describe('TransferReceiptComponent', () => {
  let component: TransferReceiptComponent;
  let fixture: ComponentFixture<TransferReceiptComponent>;
  let router: Router;
  let matDialog: MatDialog;
  let inventoryService: InventoryService;
  let rulesService: RulesService;
  let orderService: OrderService;
  let toaster: ToastrService;
  let facilityService: FacilityService;
  let transferReceiptService: TransferReceiptService;

  const order: OrderDto = {
    billingCustomerId: 1,
    comments: 'comments',
    createDate: '2023-07-19',
    deliveryType: 'deliveryType',
    desireShippingDate: new Date().toString(),
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
    shipmentType: 'INTERNAL',
    shippingLocationId: null,
    statusKey: 'OPEN',
    orderItems: [],
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        TransferReceiptComponent,
        TransferReceiptProductSelectionComponent,
        TransitTimeComponent,
        TemperatureInputComponent,
      ],
      imports: [
        RsaCommonsModule,
        MatStepperModule,
        MatFormFieldModule,
        MatSelectModule,
        MatInputModule,
        MatDatepickerModule,
        MatRadioModule,
        MatDividerModule,
        MatCardModule,
        MatCheckboxModule,
        MatIconModule,
        MatAutocompleteModule,
        ButtonModule,
        RippleModule,
        TableModule,
        FormsModule,
        ReactiveFormsModule,
        TreoCardModule,
        BrowserAnimationsModule,
        RouterTestingModule,
        HttpClientModule,
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
        { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] },
        { provide: MAT_DATE_FORMATS, useValue: MAT_MOMENT_DATE_FORMATS },
        getAppIconsTokenMockProvider(allDistributionIcons),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                transferReceipt: {
                  lookups: <LookUpDto[]>[
                    {
                      id: 1,
                      active: true,
                      descriptionKey: 'abp.label',
                      type: IMPORTS_BLOOD_TYPES,
                      optionValue: 'ABP',
                    },
                    {
                      id: 2,
                      active: true,
                      descriptionKey: 'on.label',
                      type: IMPORTS_BLOOD_TYPES,
                      optionValue: 'ON',
                    },
                  ],
                },
              },
            },
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContextAndIcons(TransferReceiptComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    router = TestBed.inject(Router);
    matDialog = TestBed.inject(MatDialog);
    inventoryService = TestBed.inject(InventoryService);
    rulesService = TestBed.inject(RulesService);
    orderService = TestBed.inject(OrderService);
    toaster = TestBed.inject(ToastrService);
    facilityService = TestBed.inject(FacilityService);
    transferReceiptService = TestBed.inject(TransferReceiptService);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open confirmation dialog on cancel and navigate to distribution module landing page', () => {
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: {
        confirmMessage: 'are-you-sure-want-cancel-message.label',
        title: 'confirmation.label',
        acceptTitle: 'confirm.label',
      },
      afterClosed: () => of(true),
    } as MatDialogRef<ConfirmDialogComponent>);
    spyOn(router, 'navigateByUrl');

    component.cancel();
    expect(matDialog.open).toBeCalled();
    expect(router.navigateByUrl).toBeCalledWith('/home');
  });

  it('should validate order number happy path', () => {
    spyOn(orderService, 'getOrderByCriteria').and.returnValue(of({ body: [order] } as HttpResponse<OrderDto[]>));
    spyOn(rulesService, 'evaluation').and.returnValue(
      of({
        body: {
          notifications: [
            {
              statusCode: '200',
              notificationType: 'success',
              message: 'rul-0116-transfer-receipt-eligibility-rules.success.label',
              notificationEventOnDismiss: 'null',
            },
          ],
          results: {
            reason: ['transfer-receipt-eligibility-rules.success.label'],
          },
          ruleCode: 'OK',
        },
      })
    );

    let event;
    event = {
      target: {
        value: '2121',
      },
    };
    component.transferOrderNumberBlur(event as FocusEvent);
    expect(orderService.getOrderByCriteria).toBeCalledWith({
      'orderNumber.equals': '2121',
    });
  });

  it('should validate order number - error path', () => {
    const notifications = [
      {
        message: 'order-not-found.label',
        notificationEventOnDismiss: 'null',
        notificationType: 'error',
        statusCode: '400',
      },
    ];
    spyOn(toaster, 'error');
    spyOn(orderService, 'getOrderByCriteria').and.returnValue(of({ body: [] } as HttpResponse<OrderDto[]>));
    spyOn(rulesService, 'evaluation').and.returnValue(
      of({
        body: {
          notifications,
          ruleCode: 'OK',
        },
      })
    );

    let event;
    event = {
      target: {
        value: '2121',
      },
    };
    component.transferOrderNumberBlur(event as FocusEvent);

    expect(toaster.error).toBeCalledWith(notifications[0].message);
  });

  it('should validate step1NextClick', () => {
    const notifications = [
      {
        message: 'rul-0116-temperature-unacceptable.label',
        notificationEventOnDismiss: 'null',
        notificationType: 'warning',
        statusCode: '400',
      },
    ];
    spyOn(toaster, 'error');
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications,
          ruleCode: 'OK',
        },
      })
    );
    spyOn(facilityService, 'getFacilityById').and.returnValue(
      of({
        body: {
          active: true,
          id: 3,
          name: 'Miami',
        },
      })
    );
    component.currentOrder = order;
    component.labelingProductCategory = ORDER_PRODUCT_CATEGORY_REFRIGERATED;
    component.ngOnInit();

    component.transferInfoGroup.patchValue({
      temperature: '+12',
      transitTime: {
        transitStartDate: '2022-01-01',
        transitStartTimeHours: '01',
        transitStartTimeMinutes: '01',
        transitStartTimezone: 'America/Chicago',
        transitEndDate: '2022-01-01',
        transitEndTimeHours: '01',
        transitEndTimeMinutes: '02',
        transitEndTimezone: 'America/Chicago',
      },
    });
    component.transferInfoGroup.updateValueAndValidity();

    component.step1NextClick();

    expect(toaster.error).toBeCalledWith(notifications[0].message);
  });
});
