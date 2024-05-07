import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormControl,
  FormGroup,
  NgForm,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatSelectChange } from '@angular/material/select';
import { MatHorizontalStepper } from '@angular/material/stepper';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import {
  BarcodeService,
  ControlErrorsDirective,
  DefaultErrorStateMatcher,
  Description,
  FacilityService,
  ImportDto,
  ImportItemConsequenceDto,
  ImportItemDto,
  InventoryService,
  LookUpDto,
  ProcessHeaderService,
  RsaValidators,
  TransitTimeRequestDto,
  ValidateRuleDto,
  ValidationType,
} from '@rsa/commons';
import {
  AddProductRuleRequest,
  AddProductRuleResult,
  ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
  CMV_STATUS,
  COMPLETED_STATUS,
  HBS_NEGATIVE,
  ImporItemAttribute,
  ImportFacilityIdentification,
  ImportItem,
  ImportItemConsequence,
  IMPORTS_BLOOD_TYPES,
  IMPORTS_INFORMATION_VALIDATION_RULE,
  IMPORTS_INSPECTION_STATUS,
  IMPORTS_TRANSIT_TIME_ZONE,
  IN_PROCESS_STATUS,
  LICENSE_STATUS,
  Patient,
  PENDING_STATUS,
  QUARANTINE_CONSEQUENCE_TYPE,
  RESULT_PROPERTY_INSPECTION_FIELD,
  RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
} from '@rsa/distribution/core/models/imports.models';
import {
  ORDER_PRODUCT_CATEGORY,
  ORDER_PRODUCT_CATEGORY_REFRIGERATED,
  ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE,
} from '@rsa/distribution/core/models/orders.model';
import { startCase } from 'lodash';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { Table } from 'primeng/table';
import { Observable, timer } from 'rxjs';
import { distinctUntilChanged, finalize, map, startWith, switchMap, takeWhile } from 'rxjs/operators';
import { PatientSearchComponent } from './patient-search/patient-search.component';
import { ImportStatusModalComponent } from './status/status.component';

@Component({
  selector: 'rsa-imports',
  templateUrl: './imports.component.html',
  styleUrls: ['./imports.component.scss'],
  providers: [{ provide: ErrorStateMatcher, useClass: DefaultErrorStateMatcher }],
})
export class ImportsComponent implements OnInit {
  readonly validationType = ValidationType;
  readonly COMMENTS_MAX_LENGTH = 1000; //maxLength on the database
  readonly importInfoWidgetTitle = 'import-information.label';
  readonly importInfoWidgetSubtitle = 'import-information.label';
  readonly licensed = 'licensed.label';
  readonly unlicensed = 'unlicensed.label';

  @ViewChild('stepper') stepper: MatHorizontalStepper;
  @ViewChild('productTable') productTable: Table;
  @ViewChild('importInfoForm') importInfoForm: NgForm;
  @ViewChild('productSelectionForm') productSelectionForm: NgForm;
  @ViewChild('controlErrorsDirective') unitNumberControlErrorsDirective: ControlErrorsDirective;

  importsGroup: FormGroup;
  productGroup: FormGroup;
  commentForm: FormGroup;
  timeZones: LookUpDto[] = [];
  productCategories: LookUpDto[] = [];
  inspections: LookUpDto[] = [];
  bloodTypes: LookUpDto[] = [];
  filteredBloodTypes: Observable<LookUpDto[]>;
  importFacilities: ImportFacilityIdentification[] = [];
  importedProducts: ImportItem[] = [];
  selectedSignOfTemperature: '+' | '-' = '+';
  transitTimeRequest: TransitTimeRequestDto;
  transitTimeColor: string;
  transitResponseStatusKey: string;
  importInformation: Description[] = [];
  productSelectionStep = false;
  showExtraFields = false;
  showTransitTime = false;
  unitNumberFocus = false;
  aboRhFocus = false;
  productCodeFocus = false;
  expirationDateFocus = false;
  loading = false;
  patientRecordNeeded = false;
  registrationNumberNeeded = false;
  consequences: ImportItemConsequence[] = [];
  selectedLabelingProductCategory: LookUpDto;

  constructor(
    public header: ProcessHeaderService,
    protected fb: FormBuilder,
    private toaster: ToastrService,
    private matDialog: MatDialog,
    private _router: Router,
    private route: ActivatedRoute,
    private inventoryService: InventoryService,
    private facilityService: FacilityService,
    private barcodeService: BarcodeService,
    private translateService: TranslateService,
    private cdr: ChangeDetectorRef
  ) {
    this.importsGroup = fb.group({
      labelingProductCategoryValue: [null, Validators.required],
      inspection: [null, Validators.required],
    });

    this.productGroup = fb.group({
      unitNumber: [null, [Validators.required, RsaValidators.unitNumber]],
      aboRh: ['', [Validators.required, this.bloodTypeValidator()]],
      productCode: [null, [Validators.required, RsaValidators.fullProductCode]],
      expirationDate: [null, [Validators.required, this.dateValid()]],
      licenseStatus: [this.unlicensed, Validators.required],
      cmvStatus: null,
      hsbNegative: false,
    });

    this.commentForm = fb.group({
      comments: [null, Validators.maxLength(this.COMMENTS_MAX_LENGTH)],
    });

    this.filteredBloodTypes = this.productGroup.controls.aboRh.valueChanges.pipe(
      startWith(''),
      distinctUntilChanged(),
      map(value => (typeof value === 'string' ? value : value?.descriptionKey)), // Added because of `displayFn`, which makes the `valueChanges` to be called again.
      map(value =>
        this.bloodTypes.filter(option =>
          this.translateService.instant(option.descriptionKey).toLowerCase().includes(value?.toLowerCase())
        )
      )
    );
  }

  ngOnInit(): void {
    if (this.route.snapshot?.data?.importsData) {
      const importsData = this.route.snapshot.data.importsData;

      this.productCategories = importsData.lookups.filter(lookUp => lookUp.type === ORDER_PRODUCT_CATEGORY);
      this.timeZones = importsData.lookups.filter(lookUp => lookUp.type === IMPORTS_TRANSIT_TIME_ZONE);
      this.inspections = importsData.lookups.filter(lookUp => lookUp.type === IMPORTS_INSPECTION_STATUS);
      this.bloodTypes = importsData.lookups.filter(lookUp => lookUp.type === IMPORTS_BLOOD_TYPES);
    }
  }

  cancel() {
    this._router.navigateByUrl('/home');
  }

  stepSelectionChange($event: StepperSelectionEvent) {
    this.productSelectionStep = $event.selectedIndex === 1;
    if (this.productSelectionStep) {
      this.step1NextClick();
    } else {
      this.resetProductGroupForm();
    }
  }

  getLookUpDescriptionKey(optionValue: string, list: LookUpDto[]): string {
    return list.find(l => l.optionValue === optionValue)?.descriptionKey ?? '';
  }

  private showToaster(notification = { message: 'something-went-wrong.label', notificationType: 'error' }) {
    this.toaster.show(
      this.translateService.instant(notification.message),
      startCase(notification.notificationType),
      {},
      notification.notificationType
    );
  }

  resetProcess() {
    this.resetImportGroupForm();
    this.resetProductGroupForm();
    this.commentForm.reset();
    this.productTable.reset();
    this.stepper.reset();
    this.consequences = [];
    this.importedProducts = [];
    this.importInformation = [];
    this.importFacilities = [];
    this.selectedSignOfTemperature = '+';
    this.transitTimeRequest = null;
    this.transitTimeColor = null;
    this.transitResponseStatusKey = null;
    this.productSelectionStep = false;
    this.patientRecordNeeded = false;
    this.loading = false;
    this.showExtraFields = false;
    this.showTransitTime = false;
    this.selectedLabelingProductCategory = null;
    this.unitNumberFocus = false;
    this.aboRhFocus = false;
    this.productCodeFocus = false;
    this.expirationDateFocus = false;
    this.registrationNumberNeeded = false;
  }

  //#region STEP 1

  step1NextClick() {
    if (this.importsGroup.valid) {
      this.productGroup.patchValue({
        licenseStatus: this.unlicensed,
      });
      this.inventoryService.validate(this.importsInfoValidateDto).subscribe(
        response => {
          const value = response.body;

          if (value.ruleCode !== 'BAD_REQUEST') {
            for (const notification of value.notifications) {
              this.showToaster(notification);
            }

            this.consequences =
              value.results?.consequences?.filter(con => con.consequenceType === QUARANTINE_CONSEQUENCE_TYPE) ?? [];

            const inspectionFailed =
              this.consequences?.some(con => con.resultProperty === RESULT_PROPERTY_INSPECTION_FIELD) ?? false;

            this.updateImportInfoWidget(inspectionFailed);
            this.unitNumberFocus = true;
          } else {
            this.showToaster();
          }
        },
        err => {
          this.showToaster();
          throw err;
        }
      );
    }
  }

  labelingProductCategoryChange(event: MatSelectChange) {
    if (event?.value !== this.selectedLabelingProductCategory) {
      this.importedProducts = [];
      this.transitTimeRequest = null;
      this.checkExtraFields(event.value.optionValue);
    }

    this.selectedLabelingProductCategory = event.value;
  }

  checkExtraFields(categoryOptionValue: string) {
    this.showExtraFields = false;
    this.showTransitTime = false;
    this.removeExtraFields();

    if (
      categoryOptionValue === ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE ||
      categoryOptionValue === ORDER_PRODUCT_CATEGORY_REFRIGERATED
    ) {
      this.showExtraFields = true;
      this.importsGroup.addControl('temperature', new FormControl('', Validators.required));

      if (categoryOptionValue === ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE) {
        this.showTransitTime = true;
        this.importsGroup.addControl('transitTime', new FormControl('', Validators.required));
      }
    }
  }

  removeExtraFields() {
    this.importsGroup.removeControl('temperature');
    this.importsGroup.removeControl('transitTime');
    this.cdr.detectChanges();
  }

  updateTransitTime(event: {
    transitTimeRequest: TransitTimeRequestDto;
    transitTime: string;
    transitResponseStatusKey: string;
    transitTimeColor: string;
  }) {
    this.transitTimeRequest = event?.transitTimeRequest;
    this.transitTimeColor = event?.transitTimeColor;
    this.transitResponseStatusKey = event?.transitResponseStatusKey;
    this.importsGroup.patchValue({ transitTime: event?.transitTime });
  }

  onToggleTemperature(signal: '+' | '-') {
    this.selectedSignOfTemperature = signal;
  }

  get importsInfoValidateDto() {
    return <ValidateRuleDto>{
      ruleName: IMPORTS_INFORMATION_VALIDATION_RULE,
      productCategory: this.importsGroup.get('labelingProductCategoryValue').value.descriptionKey,
      temperature: this.importsGroup.get('temperature')
        ? +`${this.selectedSignOfTemperature}${this.importsGroup.get('temperature').value}`
        : '',
      visualInspectKey: this.importsGroup.get('inspection').value,
      isImport: true,
      ...this.transitTimeRequest,
    };
  }

  private resetImportGroupForm() {
    this.removeExtraFields();
    this.importInfoForm.resetForm();
  }

  //#endregion

  //#region STEP 2

  onUnitNumberKeyOrTab(event: Event) {
    event.preventDefault();
    this.unitNumberFocus = false;
    this.aboRhFocus = true;
    this.onEnterKeyOrTab('unitNumber');
  }

  onBloodTypeKeyOrTab(event?: Event) {
    event?.preventDefault();
    this.aboRhFocus = false;
    this.productCodeFocus = true;
    this.onEnterKeyOrTab('aboRh');
  }

  onProductCodeKeyOrTab(event: Event) {
    event.preventDefault();
    this.productCodeFocus = false;
    this.expirationDateFocus = true;
    this.onEnterKeyOrTab('productCode');
  }

  onExpirationDateKeyOrTab(event: Event) {
    event.preventDefault();
    this.onEnterKeyOrTab('expirationDate');
  }

  onEnterKeyOrTab(controlName: string) {
    const value = this.productGroup.get(controlName).value;
    if (value && typeof value === 'string') {
      this.barcodeService.getBarcodeTranslation(value).subscribe(response => {
        const translations = response?.body?.barcodeTranslation;
        const translationKeys = Object.keys(translations);

        if (translationKeys.includes('aboRh')) {
          translations['aboRh'] = this.getBloodType(translations['aboRh']);
        }

        if (translationKeys.includes('expirationDate')) {
          translations['expirationDate'] = moment(translations['expirationDate']).format('MM/DD/YYYY');
        }

        this.productGroup.patchValue(translations);

        this.unitNumberFocus = false;
        this.aboRhFocus = translationKeys.includes('unitNumber') && translationKeys.length === 1;
        this.productCodeFocus = translationKeys.includes('unitNumber') && translationKeys.length === 2;
        this.expirationDateFocus = translationKeys.includes('productCode') && translationKeys.length === 1;
      });
    }
  }

  addProduct() {
    if (this.productGroup.valid) {
      this.expirationDateFocus = false;
      this.unitNumberFocus = true;

      if (!this.hasDuplicateProduct) {
        this.loading = true;

        this.inventoryService
          .validate(this.addProductValidationDto)
          .pipe(
            finalize(() => {
              if (!this.registrationNumberNeeded) {
                this.resetProductGroupForm();
              } else {
                this.loading = false;
              }
            })
          )
          .subscribe(
            response => {
              const value = response.body;

              if (value.ruleCode !== 'BAD_REQUEST') {
                for (const notification of value.notifications) {
                  this.showToaster(notification);
                }

                const addProductRuleResult = value.results as AddProductRuleResult;
                const importFacility = this.checkImportFacilityValue(
                  addProductRuleResult?.importFacilityIdentification?.length
                    ? addProductRuleResult.importFacilityIdentification[0]
                    : []
                );

                const importItems = addProductRuleResult?.importItem;
                if (importItems?.length) {
                  const importItem = importItems[0];
                  importItem.id = this.importedProducts.length;

                  if (importItem.patientRecord) {
                    this.patientRecordNeeded = true;
                  }

                  if (importFacility) {
                    importItem.facilityIdentification = {
                      ...importFacility,
                    };
                  }

                  const products = addProductRuleResult?.product;
                  if (products?.length) {
                    const product = products[0];

                    if (product?.descriptionKey) {
                      importItem.descriptionKey = product.descriptionKey;
                    }
                  }

                  this.setProducts([...this.importedProducts, importItem]);
                }
              } else {
                this.showToaster();
              }
            },
            err => {
              this.showToaster();
              this.resetProductGroupForm();
              throw err;
            }
          );
      } else {
        this.resetProductGroupForm();
      }
    }
  }

  checkImportFacilityValue(importFacilities: ImportFacilityIdentification[]) {
    this.importFacilities = importFacilities ?? [];
    if (this.importFacilities.length > 1) {
      this.registrationNumberNeeded = true;
      this.productGroup.addControl('registrationNumber', new FormControl(null, Validators.required));
    } else if (this.importFacilities.length === 1) {
      this.registrationNumberNeeded = false;
      return this.importFacilities[0];
    }

    return;
  }

  removeProd(index) {
    this.importedProducts.splice(index, 1);
    if (!this.importedProducts.some(prod => prod.patientRecord)) {
      this.patientRecordNeeded = false;
    }
    this.setProducts(this.importedProducts);
  }

  removeAll() {
    this.patientRecordNeeded = false;
    this.setProducts([]);
  }

  patientSearch(product: ImportItem) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '60rem';
    const dialogRef = this.matDialog.open(PatientSearchComponent, dialogConfig);
    dialogRef.componentInstance.product = product;
    dialogRef.componentInstance.patients = product.patient ? [product.patient] : [];
    dialogRef.afterClosed().subscribe((patient: Patient | null) => {
      if (patient) {
        if (product.patient) {
          this.toaster.success('patient-record-edited-successfully.label');
        } else {
          this.toaster.success('patient-record-associated-import-product.label');
        }
        product.patient = patient;
      }
    });
  }

  complete() {
    if (this.importsGroup?.valid && this.importedProducts?.length) {
      this.resetProductGroupForm();

      const dialogRef = this.matDialog.open(ImportStatusModalComponent, { disableClose: true });
      dialogRef.afterClosed().subscribe(() => {
        if (this.hasQuarantine || this.hasItemQuarantine)
          this.toaster.success('import-process-complete-quarantine.label');
        if (this.hasGoodProducts && !this.hasQuarantine) this.toaster.success('import-process-complete.label');
        this.resetProcess();
      });

      this.inventoryService
        .completeImport(this.importDto)
        .pipe(
          switchMap(importRes => {
            return timer(0, 3000).pipe(
              switchMap(() => this.inventoryService.getImportStatus(importRes.id)),
              takeWhile(statusRes => statusRes.status === IN_PROCESS_STATUS, true)
            );
          })
        )
        .subscribe(
          statusRes => {
            dialogRef.componentInstance.quantity = statusRes.totalItems;
            dialogRef.componentInstance.processed = statusRes.totalSuccess + statusRes.totalFailure;
            dialogRef.componentInstance.failures = statusRes.failures;

            if (statusRes.status === COMPLETED_STATUS) {
              dialogRef.componentInstance.canClose = true;
            }
          },
          error => {
            this.toaster.error('something-went-wrong.label');
            this.unitNumberFocus = true;
            dialogRef.close();
            throw error;
          }
        );
    }
  }

  getAttributeValue(attributes: ImporItemAttribute[], propertyKey: string) {
    return attributes.find(attr => attr.propertyKey === propertyKey)?.propertyValue ?? '';
  }

  convertAttributeValue(attr: ImporItemAttribute) {
    if (typeof attr.propertyValue === 'boolean') {
      return 'hbs-negative.label';
    }

    return attr.propertyValue;
  }

  displayBloodTypeFn = (bloodType: LookUpDto): string => {
    return bloodType?.descriptionKey ? this.translateService.instant(bloodType.descriptionKey) : '';
  }

  step2BackClick() {
    this.registrationNumberNeeded = false;
    this.resetProductGroupForm();
    this.stepper.previous();
  }

  getBloodType(value: string) {
    return this.bloodTypes.find(
      bloodType =>
        bloodType.optionValue.toLowerCase() === value?.toLowerCase() ||
        this.translateService.instant(bloodType.descriptionKey).toLowerCase() === value?.toLowerCase()
    );
  }

  get addProductValidationDto() {
    const prodFormValues = this.productGroup.value;

    const ruleDto = <AddProductRuleRequest>{
      ruleName: ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
      unitNumber: prodFormValues.unitNumber,
      bloodType: prodFormValues.aboRh.optionValue,
      isbtProductCode: prodFormValues.productCode,
      expirationDate: moment(prodFormValues.expirationDate).format('YYYY-MM-DD'),
      facilityId: this.facilityService.getFacilityId(),
      familyCategory: this.importsGroup.get('labelingProductCategoryValue')?.value?.optionValue,
      itemAttributes: [
        {
          propertyKey: LICENSE_STATUS,
          propertyValue: prodFormValues.licenseStatus,
        },
      ],
      importFacilityIdentificationId: prodFormValues.registrationNumber,
    };

    if (prodFormValues.cmvStatus) {
      ruleDto.itemAttributes.push({
        propertyKey: CMV_STATUS,
        propertyValue: prodFormValues.cmvStatus,
      });
    }

    if (prodFormValues.hsbNegative) {
      ruleDto.itemAttributes.push({
        propertyKey: HBS_NEGATIVE,
        propertyValue: prodFormValues.hsbNegative,
      });
    }

    return ruleDto;
  }

  get hasDuplicateProduct() {
    const { unitNumber, productCode } = this.productGroup.value;
    return this.importedProducts.some(prod => prod.unitNumber === unitNumber && prod.isbtProductCode === productCode);
  }

  get today() {
    return new Date();
  }

  get importDto() {
    const formValues = this.importsGroup.value;
    return <ImportDto>{
      locationId: this.facilityService.getFacilityId(),
      temperature: formValues.temperature ? `${this.selectedSignOfTemperature}${formValues.temperature}` : null,
      comments: this.commentForm?.controls?.comments?.value ?? null,
      productCategory: formValues.labelingProductCategoryValue.descriptionKey,
      shipmentInspectKey: formValues.inspection,
      totalTransitTime: formValues.transitTime ?? null,
      transitStartDateTime: this.transitTimeRequest
        ? new Date(
            `${this.transitTimeRequest.transitStartDate} ${this.transitTimeRequest.transitStartTime}`
          ).toISOString()
        : null,
      transitEndDateTime: this.transitTimeRequest
        ? new Date(`${this.transitTimeRequest.transitEndDate} ${this.transitTimeRequest.transitEndTime}`).toISOString()
        : null,
      transitTimezone:
        this.timeZones?.find(timezone => timezone.optionValue === this.facilityService.getFacilityProperty('TZ'))
          ?.optionValue ?? null,
      transitTimeResultKey: this.transitResponseStatusKey ?? null,
      importItems: this.importedProducts?.map(prod => {
        const licenseStatus = this.getAttributeValue(prod.itemAttributes, LICENSE_STATUS);
        return <ImportItemDto>{
          unitNumber: prod.unitNumber,
          productCode: prod.isbtProductCode,
          bloodType: prod.bloodType,
          productConsequenceKey:
            prod.returnItemConsequences?.length || this.hasQuarantine
              ? QUARANTINE_CONSEQUENCE_TYPE
              : RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
          licenseStatus: licenseStatus,
          expirationDate: new Date(prod.expirationDate).toISOString(),
          licenseNumber: licenseStatus === this.licensed ? prod.facilityIdentification?.licenseNumber : null,
          registrationNumber: prod.facilityIdentification?.registrationNumber,
          status: PENDING_STATUS,
          patientId: prod.patient?.id,
          importItemAttributes: prod.itemAttributes,
          importItemConsequences: [
            ...[...this.consequences, ...(prod.returnItemConsequences ?? [])].map(con => {
              return <ImportItemConsequenceDto>{
                itemConsequenceReasonKey: con.consequenceReasonKey,
                itemConsequenceType: con.consequenceType,
              };
            }),
          ],
        };
      }),
    };
  }

  get isCompleteDisabled() {
    return (
      !this.importsGroup.valid ||
      !this.importedProducts.length ||
      this.importedProducts.some(prod => prod.patientRecord && !prod.patient)
    );
  }

  get addProductVisible() {
    const controls = this.productGroup.controls;
    return (
      controls.unitNumber.value &&
      controls.unitNumber.valid &&
      controls.aboRh.value &&
      controls.aboRh.valid &&
      controls.productCode.value &&
      controls.productCode.valid &&
      controls.expirationDate.value &&
      controls.expirationDate.valid &&
      (controls.registrationNumber ? controls.registrationNumber.value && controls.registrationNumber.valid : true)
    );
  }

  get hasQuarantine() {
    return !!this.consequences?.length;
  }

  get hasItemQuarantine() {
    return this.importedProducts.some(prod => prod.returnItemConsequences?.length);
  }

  get hasGoodProducts() {
    return this.importedProducts.some(prod => !prod.returnItemConsequences?.length);
  }

  private updateImportInfoWidget(inspectionFailed: boolean) {
    const formValues = this.importsGroup.value;
    this.importInformation = [
      { label: 'labeling-product-category.label', value: formValues.labelingProductCategoryValue.descriptionKey },
      {
        label: 'inspection.label',
        value: [this.getLookUpDescriptionKey(formValues.inspection, this.inspections)],
        valueType: 'badge',
        valueCls: 'badge',
        valueStyle: [
          {
            'background-color': inspectionFailed ? '#f65151' : '#4caf50',
            color: 'white',
          },
        ],
      },
    ];

    if (this.showExtraFields) {
      if (this.showTransitTime) {
        this.importInformation.splice(3, 0, {
          label: 'transit-time.label',
          value: formValues.transitTime,
          valueStyle: { color: this.transitTimeColor },
        });
      }
      this.importInformation.push({
        label: 'temperature.label',
        value: `${this.selectedSignOfTemperature}${formValues.temperature}${this.translateService.instant(
          'celsius.label'
        )}`,
      });
    }
  }

  private setProducts(products: ImportItem[]) {
    this.importedProducts = products;
    this.productTable?.reset();
  }

  private resetProductGroupForm() {
    this.loading = false;

    if (!this.registrationNumberNeeded) {
      this.productGroup.removeControl('registrationNumber');
    }

    this.productSelectionForm.resetForm();
    this.productGroup.reset({ licenseStatus: this.unlicensed, aboRh: '' });
    this.unitNumberControlErrorsDirective.hideErrorMessage();
    this.productGroup.controls.unitNumber.setErrors(null);
    this.productGroup.controls.aboRh.setErrors(null);
    this.productGroup.controls.productCode.setErrors(null);
    this.productGroup.controls.expirationDate.setErrors(null);
  }

  private bloodTypeValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const bloodTypeValue = control?.value ?? '';

      if (bloodTypeValue && !this.bloodTypes.some(bt => bt.optionValue === bloodTypeValue.optionValue)) {
        return { invalid: true };
      }

      return null;
    };
  }

  private dateValid(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const dateValue = control?.value ?? '';

      if (control?.hasError('matDatepickerParse') || (dateValue && !moment(dateValue, 'MM/DD/YYYY', true).isValid())) {
        return { invalidDate: true };
      }

      return null;
    };
  }

  //#endregion
}
