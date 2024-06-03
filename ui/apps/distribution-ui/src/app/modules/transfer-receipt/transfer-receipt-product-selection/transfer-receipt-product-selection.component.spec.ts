import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_MOMENT_DATE_FORMATS, MomentDateAdapter } from '@angular/material-moment-adapter';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule, MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  createTestContextAndIcons,
  getAppIconsTokenMockProvider,
  getAppInitializerMockProvider,
  InventoryService,
  LabelStatus,
  LookUpDto,
  ProductSelectionItemDto,
  RsaCommonsModule,
  RuleInputsDto,
  RuleResponseDto,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { IMPORTS_BLOOD_TYPES } from '@rsa/distribution/core/models/imports.models';
import { TransferReceiptProductSelectionComponent } from '@rsa/distribution/modules/transfer-receipt/transfer-receipt-product-selection/transfer-receipt-product-selection.component';
import {
  BARCODE_VALIDATOR,
  FAILED_BARCODE_RESPONSE,
  FAILED_RULE_RESPONSE,
  INVENTORY_DESCRIPTION_KEY_REF,
  INVENTORY_ID_REF,
  ISBT_PRODUCT_CODE_REF,
  PRODUCT_CODE_SCANNED_REF,
  SUCCESS_RULE_115_MULTIPLE_PRODUCTS_RESPONSE,
  SUCCESS_RULE_115_RESPONSE,
  SUCCESS_RULE_117_RESPONSE,
  SUCCESS_UNIT_NUMBER_BARCODE_RESPONSE,
  UNIT_NUMBER_REF,
  UNIT_NUMBER_SCANNED_REF,
} from '@rsa/distribution/modules/transfer-receipt/transfer-receipt-product-selection/transfer-receipt.test.mock';
import { MatDialogRefMock } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of, throwError } from 'rxjs';
import { allDistributionIcons } from '../../../../icons';

describe('TransferReceiptProductSelectionComponent', () => {
  let component: TransferReceiptProductSelectionComponent;
  let fixture: ComponentFixture<TransferReceiptProductSelectionComponent>;
  let inventoryService: InventoryService;

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
    productCategoryKey: '',
    shippingCustomerId: 0,
    shippingMethod: '',
    statusKey: '',
    id: 0,
    shipmentType: 'INTERNAL_TRANSFER',
    labelStatus: labelStatus,
    createDate: '2023-07-12T00:00:00.000',
    orderItems: [],
  });

  const successNotification = {
    statusCode: '200',
    notificationType: 'success',
    message: 'Success',
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TransferReceiptProductSelectionComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
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
          provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
          useValue: { floatLabel: 'always' },
        },
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
    const testContext = createTestContextAndIcons(TransferReceiptProductSelectionComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    inventoryService = TestBed.inject(InventoryService);
    fixture.detectChanges();
    component.products = [];
    component.currentOrder = mockOrder('LABELED');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should add a product if not duplicated', () => {
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        status: 200,
        body: { notifications: [successNotification] },
      })
    );
    spyOn(component.rsaOnProductSelectionChange, 'emit');
    component.addProduct(
      createProduct({
        unitNumber: 'W123456789012',
      })
    );
    expect(component.products.length).toEqual(1);
    component.addProduct(
      createProduct({
        unitNumber: 'W123456789012',
      })
    );
    expect(component.products.length).toEqual(1);
    expect(component.rsaOnProductSelectionChange.emit).toHaveBeenCalledTimes(1);
  });

  it('should emit rsa-on-product-selection', () => {
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        status: 200,
        body: { notifications: [successNotification] },
      })
    );
    spyOn(component.rsaOnProductSelectionChange, 'emit');
    component.addProduct(
      createProduct({
        unitNumber: 'W123456789012',
      })
    );
    expect(component.products.length).toEqual(1);
    expect(component.rsaOnProductSelectionChange.emit).toHaveBeenCalled();
  });

  it('should remove item on click remove button', () => {
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        status: 200,
        body: { notifications: [successNotification] },
      })
    );
    spyOn(component.rsaOnProductSelectionChange, 'emit');
    const product1 = createProduct({
      unitNumber: 'W123456789012',
    });
    const product2 = createProduct({
      id: 4321,
      unitNumber: 'W123456789012',
    });
    component.addProduct(product1);
    component.addProduct(product2);
    expect(component.products.length).toEqual(2);
    expect(component.rsaOnProductSelectionChange.emit).toHaveBeenCalled();

    component.remove(product1);
    expect(component.products.length).toEqual(1);
    expect(component.rsaOnProductSelectionChange.emit).toHaveBeenCalledTimes(3);
  });

  it('should remove all items on remove all items button', () => {
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        status: 200,
        body: { notifications: [successNotification] },
      })
    );
    spyOn(component.rsaOnProductSelectionChange, 'emit');
    const product1 = createProduct({
      unitNumber: 'W123456789012',
    });
    const product2 = createProduct({
      id: 4321,
      unitNumber: 'W123456789012',
    });
    component.addProduct(product1);
    component.addProduct(product2);
    expect(component.products.length).toEqual(2);
    expect(component.rsaOnProductSelectionChange.emit).toHaveBeenCalled();

    component.removeAll();
    expect(component.products.length).toEqual(0);
    expect(component.rsaOnProductSelectionChange.emit).toHaveBeenCalledTimes(3);
  });

  it(`should translate unit number barcode`, async () => {
    component.productSelectionGroup.patchValue({
      unitNumber: UNIT_NUMBER_SCANNED_REF,
    });
    spyOn(component, 'updateUnitNumberInputValue');
    spyOn(component.barcodeService, 'getBarcodeTranslation').and.returnValue(of(SUCCESS_UNIT_NUMBER_BARCODE_RESPONSE));
    spyOn(inventoryService, 'validate').and.callFake(
      mockRules({
        'rul-0115-get-products-by-unit-number-and-status': FAILED_RULE_RESPONSE,
        'rul-0117-transfer-receipt-product-selected-validation': SUCCESS_RULE_117_RESPONSE,
      })
    );
    component.onUnitNumberKeyOrTab();
    expect(component.updateUnitNumberInputValue).toHaveBeenCalledWith(UNIT_NUMBER_REF);
  });

  it(`should translate product code barcode`, async () => {
    component.currentOrder.labelStatus = 'LABELED';
    component.productSelectionGroup.patchValue({
      unitNumber: UNIT_NUMBER_SCANNED_REF,
      productCode: PRODUCT_CODE_SCANNED_REF,
    });
    spyOn(component, 'updateUnitNumberInputValue');
    spyOn(component, 'updateProductCodeInputValue');
    spyOn(component.barcodeService, 'getBarcodeTranslation').and.callFake(BARCODE_VALIDATOR);
    spyOn(inventoryService, 'validate').and.callFake(
      mockRules({
        'rul-0115-get-products-by-unit-number-and-status': FAILED_RULE_RESPONSE,
        'rul-0117-transfer-receipt-product-selected-validation': SUCCESS_RULE_117_RESPONSE,
      })
    );
    component.onUnitNumberKeyOrTab();
    component.onProductCodeKeyOrTab();
    expect(component.updateUnitNumberInputValue).toHaveBeenCalledWith(UNIT_NUMBER_REF);
    expect(component.updateProductCodeInputValue).toHaveBeenCalledWith(ISBT_PRODUCT_CODE_REF);
  });

  it(`should display toast when failed rule for unlabeled product`, () => {
    component.currentOrder.labelStatus = 'UNLABELED';
    spyOn(component, 'barcodeTranslate').and.returnValue(of(FAILED_BARCODE_RESPONSE));
    spyOn(component.toaster, 'error');
    spyOn(inventoryService, 'validate').and.callFake(
      mockRules({
        'rul-0115-get-products-by-unit-number-and-status': FAILED_RULE_RESPONSE,
        'rul-0117-transfer-receipt-product-selected-validation': SUCCESS_RULE_117_RESPONSE,
      })
    );
    component.productSelectionGroup.patchValue({
      unitNumber: 'W123456789023',
    });
    component.onUnitNumberKeyOrTab();
    expect(component.products.length).toEqual(0);
    expect(component.toaster.error).toBeCalledWith('something-went-wrong.label', 'error.label');
  });

  it(`should add unlabeled product if all rules succeed`, () => {
    component.currentOrder.labelStatus = 'UNLABELED';
    spyOn(component, 'barcodeTranslate').and.returnValue(of(FAILED_BARCODE_RESPONSE));
    spyOn(inventoryService, 'validate').and.callFake(
      mockRules({
        'rul-0115-get-products-by-unit-number-and-status': SUCCESS_RULE_115_RESPONSE,
        'rul-0117-transfer-receipt-product-selected-validation': SUCCESS_RULE_117_RESPONSE,
      })
    );
    component.productSelectionGroup.patchValue({
      unitNumber: 'W123456789023',
    });
    component.onUnitNumberKeyOrTab();
    expect(component.products.length).toEqual(1);
    const product = component.products[0];
    expect(product.id).toEqual(INVENTORY_ID_REF);
    expect(product.descriptionKey).toEqual(INVENTORY_DESCRIPTION_KEY_REF);
    expect(product.isQuarantine).toBeFalsy(); // this property MUST exist and MUST be false if unset
  });

  it(`should open modal if multiple products found on rule 115`, () => {
    component.currentOrder.labelStatus = 'UNLABELED';
    spyOn(component, 'barcodeTranslate').and.returnValue(of(FAILED_BARCODE_RESPONSE));
    spyOn(component, 'openProductSelectionModal').and.callFake(products => {
      return of(products[0]);
    });
    spyOn(inventoryService, 'validate').and.callFake(
      mockRules({
        'rul-0115-get-products-by-unit-number-and-status': SUCCESS_RULE_115_MULTIPLE_PRODUCTS_RESPONSE,
        'rul-0117-transfer-receipt-product-selected-validation': SUCCESS_RULE_117_RESPONSE,
      })
    );
    component.productSelectionGroup.patchValue({
      unitNumber: 'W123456789023',
    });
    component.onUnitNumberKeyOrTab();
    expect(component.openProductSelectionModal).toHaveBeenCalled();
    expect(component.products.length).toEqual(1);
    const product = component.products[0];
    expect(product.id).toEqual(INVENTORY_ID_REF);
    expect(product.descriptionKey).toEqual(INVENTORY_DESCRIPTION_KEY_REF);
    expect(product.isQuarantine).toBeFalsy(); // this property MUST exist and MUST be false if unset
    expect(component.productSelectionGroup.get('unitNumber').value).toBe(null);
  });

  it(`should add labeled product if all rules succeed`, () => {
    component.currentOrder.labelStatus = 'LABELED';
    spyOn(component, 'barcodeTranslate').and.returnValue(of({}));
    spyOn(inventoryService, 'validate').and.callFake(
      mockRules({
        'rul-0117-transfer-receipt-product-selected-validation': SUCCESS_RULE_117_RESPONSE,
      })
    );
    component.productSelectionGroup.patchValue({
      unitNumber: 'W123456789023',
      productCode: 'E0012V00',
    });
    component.onProductCodeKeyOrTab();
    expect(component.products.length).toEqual(1);
    const product = component.products[0];
    expect(product.id).toEqual(INVENTORY_ID_REF);
    expect(product.descriptionKey).toEqual(INVENTORY_DESCRIPTION_KEY_REF);
    expect(product.isQuarantine).toBeTruthy();
    expect(component.productSelectionGroup.get('unitNumber').value).toBe(null);
    expect(component.productSelectionGroup.get('productCode').value).toBe(null);
  });

  it(`should remove donor intention part from scanned product code`, () => {
    const extracted1 = component.extractProductCode('=<E0023V00');
    expect(extracted1).toEqual('E002300');
  });

  it(`should remove donor intention part on full product code`, () => {
    const extracted2 = component.extractProductCode('E0023V00');
    expect(extracted2).toEqual('E002300');
  });

  it(`should return the same value for other product code formats`, () => {
    const extracted3 = component.extractProductCode('WHOLEBLOOD');
    expect(extracted3).toEqual('WHOLEBLOOD');
  });

  function createProduct(product: Partial<ProductSelectionItemDto>): ProductSelectionItemDto {
    return {
      unitNumber: 'W122344454545',
      productCode: 'E0023V00',
      isQuarantine: true,
      processIndex: 'created',
      currentFacilityId: 1,
      descriptionKey: 'Test Description',
      facilityId: 3,
      donationId: 123,
      notifications: '',
      status: 'available',
      createDate: new Date(),
      deleteDate: null,
      discardDate: null,
      modificationDate: new Date(),
      parentId: null,
      icon: '',
      properties: {},
      id: 1,
      ...product,
    };
  }
});

function mockRules(config: { [key: string]: Partial<HttpResponse<RuleResponseDto>> | any }) {
  return (params: RuleInputsDto) => {
    if (params.ruleName in config) return of(config[params.ruleName]);
    return throwError({ status: 400 });
  };
}
