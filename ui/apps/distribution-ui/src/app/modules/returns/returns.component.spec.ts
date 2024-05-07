import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  AutoFocusIfDirective,
  ControlErrorComponent,
  ControlErrorsDirective,
  FacilityService, getAppInitializerMockProvider,
  InventoryService,
  ProcessHeaderComponent,
  ReturnsItemConsequenceDto,
  ReturnsItemDto,
  RuleResponseDto,
  ShipmentService,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE } from '@rsa/distribution/core/models/orders.model';
import {
  ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
  QUARANTINE_CONSEQUENCE_TYPE,
  RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
} from '@rsa/distribution/core/models/returns.models';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { productListMock } from '@rsa/distribution/data/mock/returns.mock';
import { createTestContext } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { DescriptionCardComponent } from 'libs/commons/src/lib/components/information-card/description-card.component';
import { startCase } from 'lodash';
import { ToastrService } from 'ngx-toastr';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { ReturnsComponent } from './returns.component';

describe('ReturnsComponent', () => {
  let component: ReturnsComponent;
  let fixture: ComponentFixture<ReturnsComponent>;
  let router: Router;
  let inventoryService: InventoryService;
  let shipmentService: ShipmentService;
  let facilityService: FacilityService;
  let toaster: ToastrService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        ReturnsComponent,
        ProcessHeaderComponent,
        DescriptionCardComponent,
        ControlErrorComponent,
        AutoFocusIfDirective,
        ValidationPipe,
        ControlErrorsDirective,
      ],
      imports: [
        MatStepperModule,
        MatFormFieldModule,
        MatSelectModule,
        MatInputModule,
        MatDatepickerModule,
        MatRadioModule,
        MatDividerModule,
        MatCardModule,
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
        ...toasterMockProvider
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<ReturnsComponent>(ReturnsComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    router = TestBed.inject(Router);
    inventoryService = TestBed.inject(InventoryService);
    shipmentService = TestBed.inject(ShipmentService);
    facilityService = TestBed.inject(FacilityService);
    toaster = TestBed.inject(ToastrService);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should navigate to distribution module landing page', () => {
    spyOn(router, 'navigateByUrl');

    component.cancel();

    expect(router.navigateByUrl).toBeCalledWith('/home');
  });

  it('should add temperature and transit time controls to the form', () => {
    const matSelectChange = {value: {optionValue: ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE}} as MatSelectChange;

    component.labelingProductCategoryChange(matSelectChange);

    expect(component.showExtraFields).toBeTruthy();
    expect(component.showTransitTime).toBeTruthy();
    expect(component.returnOrderGroup.get('temperature')).toBeDefined();
    expect(component.returnOrderGroup.get('transitTime')).toBeDefined();
  });

  it('should call the step 1 next click', () => {
    const event = {selectedIndex: 1} as StepperSelectionEvent;

    spyOn(component, 'step1NextClick').and.callThrough();
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications: [],
          results: {
            consequences: [
              {
                consequenceType: RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
                consequenceReasonKey: '',
                productCategory: 'frozen.label',
                resultValue: '',
                resultProperty: '',
              },
            ],
          },
          ruleCode: 'OK',
        },
      } as HttpResponse<RuleResponseDto>)
    );

    component.returnOrderGroup.patchValue({
      labelingProductCategoryValue: 'frozen.label',
      returnReason: 'reason.label',
      inspection: 'acceptable.label',
    });
    component.stepSelectionChange(event);

    expect(component.step1NextClick).toBeCalled();
    expect(inventoryService.validate).toBeCalledWith(component.returnInfoValidateDto);
  });

  it('should call the step 1 next click and display warning message', () => {
    const event = {selectedIndex: 1} as StepperSelectionEvent;
    const notification = {message: 'warning message', notificationType: 'warning', statusCode: '200'};

    spyOn(toaster, 'show');
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications: [notification],
          results: {
            consequences: [
              {
                consequenceType: QUARANTINE_CONSEQUENCE_TYPE,
                consequenceReasonKey: '',
                productCategory: 'frozen.label',
                resultValue: '',
                resultProperty: '',
              },
            ],
          },
          ruleCode: 'OK',
        },
      } as HttpResponse<RuleResponseDto>)
    );

    component.returnOrderGroup.patchValue({
      labelingProductCategoryValue: 'frozen.label',
      returnReason: 'reason.label',
      inspection: 'acceptable.label',
    });
    component.stepSelectionChange(event);

    expect(toaster.show).toBeCalledWith(
      'warning message',
      startCase(notification.notificationType),
      {},
      notification.notificationType
    );
  });

  it('should confirm the return', () => {
    const facilityId = 1;
    const form = {
      labelingProductCategoryValue: {descriptionKey: 'frozen.label'},
      returnReason: 'reason.label',
      inspection: 'acceptable.label',
    };
    const products = productListMock;
    const comments = 'Comments';
    const returnDto = {
      locationId: facilityId,
      temperature: null,
      comments: comments,
      productCategory: form.labelingProductCategoryValue.descriptionKey,
      returnNumber: null,
      returnReasonKey: form.returnReason,
      shipmentInspectKey: form.inspection,
      totalTransitTime: null,
      transitStartDateTime: null,
      transitEndDateTime: null,
      transitTimeZone: null,
      transitTimeResultKey: null,
      returnsItems: products?.map(prod => {
        return <ReturnsItemDto>{
          inventoryId: prod.inventoryId,
          productCode: prod.isbtProductCode,
          unitNumber: prod.unitNumber,
          productConsequenceKey: RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
          returnsItemConsequences: [
            ...[...component.consequences, ...(prod.returnItemConsequences ?? [])].map(con => {
              return <ReturnsItemConsequenceDto>{
                itemConsequenceReasonKey: con.consequenceReasonKey,
                itemConsequenceType: con.consequenceType,
              };
            }),
          ],
        };
      }),
    };

    spyOn(facilityService, 'getFacilityId').and.returnValue(facilityId);
    spyOn(shipmentService, 'confirmReturn').and.returnValue(of({}));
    spyOn(toaster, 'success');

    component.returnOrderGroup.patchValue({
      ...form,
    });
    component.commentForm.patchValue({comments});
    component.products = products;
    component.onClickSubmit();

    expect(facilityService.getFacilityId).toBeCalled();
    expect(shipmentService.confirmReturn).toBeCalledWith(returnDto);
    expect(toaster.success).toBeCalledWith('return-process-complete.label');
  });

  it('should add a product', () => {
    const facilityId = 1;
    const productGroup = {
      unitNumber: productListMock[0].unitNumber,
      productCode: productListMock[0].isbtProductCode,
    };

    spyOn(facilityService, 'getFacilityId').and.returnValue(facilityId);
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications: [],
          results: {
            returnItem: [productListMock[0]],
          },
          ruleCode: 'OK',
        },
      } as HttpResponse<RuleResponseDto>)
    );

    component.returnOrderGroup.patchValue({
      labelingProductCategoryValue: {optionValue: ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE},
    });
    component.productGroup.patchValue({...productGroup});
    component.onProductCodeKeyOrTab();

    expect(inventoryService.validate).toBeCalledWith({
      ruleName: ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
      unitNumber: productGroup.unitNumber,
      isbtProductCode: productGroup.productCode,
      familyCategory: ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE,
    });
    expect(component.rareDonorExists).toBeTruthy();
    expect(component.quarantineExists).toBeTruthy();
    expect(component.products.length).toEqual(1);
  });

  it('should remove product', () => {
    component.products = [...productListMock];
    component.removeProd(0);

    expect(component.products.length).toEqual(productListMock.length - 1);
  });

  it('should remove all products', () => {
    component.products = productListMock;
    component.removeAll();

    expect(component.products.length).toEqual(0);
  });
});
