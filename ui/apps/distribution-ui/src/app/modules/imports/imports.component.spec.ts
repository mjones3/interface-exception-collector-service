import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
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
import { MatSelectChange, MatSelectModule } from '@angular/material/select';
import { MatStepperModule } from '@angular/material/stepper';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute, Router } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  AutoFocusIfDirective,
  ControlErrorComponent,
  ControlErrorsDirective,
  FacilityService, getAppInitializerMockProvider,
  ImportDto,
  ImportItemConsequenceDto,
  ImportItemDto,
  InventoryService,
  LookUpDto,
  ProcessHeaderComponent,
  RuleResponseDto,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import {
  ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
  CMV_STATUS,
  COMPLETED_STATUS,
  IMPORTS_BLOOD_TYPES,
  IN_PROCESS_STATUS,
  LICENSE_STATUS,
  PENDING_STATUS,
  QUARANTINE_CONSEQUENCE_TYPE,
  RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
} from '@rsa/distribution/core/models/imports.models';
import { ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE } from '@rsa/distribution/core/models/orders.model';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { importFacilitiesMock, patientListMock, productListMock } from '@rsa/distribution/data/mock/imports.mock';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { DescriptionCardComponent } from 'libs/commons/src/lib/components/information-card/description-card.component';
import { startCase } from 'lodash';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { ButtonModule } from 'primeng/button';
import { RippleModule } from 'primeng/ripple';
import { TableModule } from 'primeng/table';
import { of } from 'rxjs';
import { ImportsComponent } from './imports.component';
import { PatientSearchComponent } from './patient-search/patient-search.component';
import { ImportStatusModalComponent } from './status/status.component';

describe('ImportsComponent', () => {
  let component: ImportsComponent;
  let fixture: ComponentFixture<ImportsComponent>;
  let router: Router;
  let inventoryService: InventoryService;
  let facilityService: FacilityService;
  let matDialog: MatDialog;
  let toaster: ToastrService;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [
        ImportsComponent,
        ProcessHeaderComponent,
        DescriptionCardComponent,
        ControlErrorComponent,
        AutoFocusIfDirective,
        ControlErrorsDirective,
        ValidationPipe,
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
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              data: {
                importsData: {
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
    const testContext = createTestContext<ImportsComponent>(ImportsComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    router = TestBed.inject(Router);
    inventoryService = TestBed.inject(InventoryService);
    matDialog = TestBed.inject(MatDialog);
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
    const matSelectChange = { value: { optionValue: ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE } } as MatSelectChange;

    component.labelingProductCategoryChange(matSelectChange);

    expect(component.showExtraFields).toBeTruthy();
    expect(component.showTransitTime).toBeTruthy();
    expect(component.importsGroup.get('temperature')).toBeDefined();
    expect(component.importsGroup.get('transitTime')).toBeDefined();
  });

  it('should call the step 1 next click', () => {
    const event = { selectedIndex: 1 } as StepperSelectionEvent;

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

    component.importsGroup.patchValue({
      labelingProductCategoryValue: 'frozen.label',
      inspection: 'acceptable.label',
    });
    component.stepSelectionChange(event);

    expect(component.step1NextClick).toBeCalled();
    expect(inventoryService.validate).toBeCalledWith(component.importsInfoValidateDto);
  });

  it('should call the step 1 next click and display warning message', () => {
    const event = { selectedIndex: 1 } as StepperSelectionEvent;
    const notification = { message: 'warning message', notificationType: 'warning', statusCode: '200' };

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

    component.importsGroup.patchValue({
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

  //TODO: Fix it
  it.skip('should complete the import', fakeAsync(() => {
    const facilityId = 1;
    const form = {
      labelingProductCategoryValue: { descriptionKey: 'frozen.label' },
      inspection: 'acceptable.label',
    };
    const products = productListMock;
    const comments = 'Comments';
    const importDto: ImportDto = {
      locationId: facilityId,
      temperature: null,
      comments: comments,
      productCategory: form.labelingProductCategoryValue.descriptionKey,
      shipmentInspectKey: form.inspection,
      totalTransitTime: null,
      transitStartDateTime: null,
      transitEndDateTime: null,
      transitTimezone: null,
      transitTimeResultKey: null,
      importItems: products?.map(prod => {
        return <ImportItemDto>{
          unitNumber: prod.unitNumber,
          bloodType: prod.bloodType,
          productCode: prod.isbtProductCode,
          expirationDate: new Date(prod.expirationDate).toISOString(),
          licenseNumber: prod.facilityIdentification.licenseNumber,
          registrationNumber: prod.facilityIdentification.registrationNumber,
          productConsequenceKey: RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
          status: PENDING_STATUS,
          patientId: prod.patient?.id,
          licenseStatus: component.getAttributeValue(prod.itemAttributes, LICENSE_STATUS),
          importItemConsequences: [
            ...[...component.consequences, ...(prod.returnItemConsequences ?? [])].map(con => {
              return <ImportItemConsequenceDto>{
                itemConsequenceReasonKey: con.consequenceReasonKey,
                itemConsequenceType: con.consequenceType,
              };
            }),
          ],
          importItemAttributes: prod.itemAttributes.map(attr => {
            return {
              ...attr,
              propertyValue: attr.propertyValue.toString(),
            };
          }),
        };
      }),
    };

    spyOn(facilityService, 'getFacilityId').and.returnValue(facilityId);
    spyOn(inventoryService, 'completeImport').and.returnValue(of({ id: 1 }));
    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { quantity: 2, processed: 3, failures: [] },
    } as MatDialogRef<ImportStatusModalComponent>);
    spyOn(inventoryService, 'getImportStatus').and.returnValues([
      of({ totalItem: 2, totalSuccess: 1, totalFailure: 0, failures: [], status: IN_PROCESS_STATUS }),
      of({ totalItem: 2, totalSuccess: 2, totalFailure: 0, failures: [], status: COMPLETED_STATUS }),
    ]);
    spyOn(toaster, 'success');

    component.importsGroup.patchValue({
      ...form,
    });
    component.commentForm.patchValue({ comments });
    component.importedProducts = products;
    component.complete();

    tick(7000);
    expect(facilityService.getFacilityId).toBeCalled();
    expect(inventoryService.completeImport).toBeCalledWith(importDto);
    expect(matDialog.open).toBeCalled();
    expect(inventoryService.getImportStatus).toBeCalledTimes(2);
    expect(toaster.success).toBeCalledWith('import-process-complete.label');
  }));

  it('should add a product', () => {
    const facilityId = 1;
    const productGroup = {
      unitNumber: productListMock[0].unitNumber,
      aboRh: { optionValue: productListMock[0].bloodType },
      productCode: productListMock[0].isbtProductCode,
      expirationDate: productListMock[0].expirationDate,
      licenseStatus: false,
      cmvStatus: 'cmv-positive.label',
      hbsNegative: false,
    };

    spyOn(facilityService, 'getFacilityId').and.returnValue(facilityId);
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications: [],
          results: {
            importItem: [productListMock[0]],
            importFacilityIdentification: [[importFacilitiesMock[0]]],
          },
          ruleCode: 'OK',
        },
      } as HttpResponse<RuleResponseDto>)
    );

    component.importsGroup.patchValue({
      labelingProductCategoryValue: { optionValue: 'frozen.label' },
      inspection: 'acceptable.label',
    });
    component.productGroup.patchValue({ ...productGroup });
    component.addProduct();

    expect(inventoryService.validate).toBeCalledWith({
      ruleName: ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
      unitNumber: productGroup.unitNumber,
      bloodType: productGroup.aboRh.optionValue,
      isbtProductCode: productGroup.productCode,
      expirationDate: moment(productGroup.expirationDate).format('YYYY-MM-DD'),
      facilityId: facilityId,
      familyCategory: 'frozen.label',
      importFacilityIdentificationId: undefined,
      itemAttributes: [
        {
          propertyKey: LICENSE_STATUS,
          propertyValue: productGroup.licenseStatus,
        },
        {
          propertyKey: CMV_STATUS,
          propertyValue: productGroup.cmvStatus,
        },
      ],
    });
    expect(component.importedProducts.length).toEqual(1);
  });

  it('should request registration number selection', () => {
    const facilityId = 1;
    const productGroup = {
      unitNumber: productListMock[0].unitNumber,
      aboRh: { optionValue: productListMock[0].bloodType },
      productCode: productListMock[0].isbtProductCode,
      expirationDate: productListMock[0].expirationDate,
      licenseStatus: true,
    };

    spyOn(facilityService, 'getFacilityId').and.returnValue(facilityId);
    spyOn(inventoryService, 'validate').and.returnValue(
      of({
        body: {
          notifications: [],
          results: {
            importItem: [],
            importFacilityIdentification: [importFacilitiesMock],
          },
          ruleCode: 'OK',
        },
      } as HttpResponse<RuleResponseDto>)
    );

    component.importsGroup.patchValue({
      labelingProductCategoryValue: { optionValue: 'frozen.label' },
      inspection: 'acceptable.label',
    });
    component.productGroup.patchValue({ ...productGroup });
    component.addProduct();

    expect(component.importedProducts.length).toEqual(0);
    expect(component.registrationNumberNeeded).toBeTruthy();
    expect(component.productGroup.controls.registrationNumber).toBeDefined();
  });

  it('should remove product', () => {
    component.importedProducts = [...productListMock];
    component.removeProd(0);

    expect(component.importedProducts.length).toEqual(productListMock.length - 1);
  });

  it('should remove all products', () => {
    component.importedProducts = productListMock;
    component.removeAll();

    expect(component.importedProducts.length).toEqual(0);
  });

  it('should open patient search dialog', () => {
    const prod = productListMock[0];

    spyOn(matDialog, 'open').and.returnValue({
      componentInstance: { product: prod, selectedPatient: undefined },
      afterClosed: () => of(patientListMock[0]),
    } as MatDialogRef<PatientSearchComponent>);
    spyOn(toaster, 'success');

    component.patientSearch(prod);

    expect(matDialog.open).toBeCalled();
    expect(toaster.success).toBeCalledWith('patient-record-associated-import-product.label');
    expect(prod.patient).toEqual(patientListMock[0]);
  });
});
