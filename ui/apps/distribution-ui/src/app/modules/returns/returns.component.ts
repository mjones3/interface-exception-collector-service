import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, NgForm, Validators } from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { MatSelectChange } from '@angular/material/select';
import { MatHorizontalStepper } from '@angular/material/stepper';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import {
  commonRegex,
  DefaultErrorStateMatcher,
  Description,
  FacilityService,
  InventoryService,
  LookUpDto,
  ProcessHeaderService,
  ReasonDto,
  ReturnsDto,
  ReturnsItemConsequenceDto,
  ReturnsItemDto,
  RsaValidators,
  ShipmentService,
  TransitTimeRequestDto,
  ValidateRuleDto,
  ValidationType,
} from '@rsa/commons';
import {
  ORDER_PRODUCT_CATEGORY,
  ORDER_PRODUCT_CATEGORY_REFRIGERATED,
  ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE,
} from '@rsa/distribution/core/models/orders.model';
import {
  ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
  QUARANTINE_CONSEQUENCE_TYPE,
  RESULT_PROPERTY_INSPECTION_FIELD,
  ReturnItem,
  ReturnItemConsequence,
  RETURNS_INSPECTION_STATUS,
  RETURNS_TRANSIT_TIME_ZONE,
  RETURN_INFORMATION_VALIDATION_RULE,
  RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
} from '@rsa/distribution/core/models/returns.models';
import { startCase } from 'lodash';
import { ToastrService } from 'ngx-toastr';
import { Table } from 'primeng/table';
import { finalize } from 'rxjs/operators';

@Component({
  selector: 'rsa-returns',
  templateUrl: './returns.component.html',
  styleUrls: ['./returns.component.scss'],
  providers: [{ provide: ErrorStateMatcher, useClass: DefaultErrorStateMatcher }],
})
export class ReturnsComponent implements OnInit {
  readonly validationType = ValidationType;
  readonly RETURN_COMMENTS_MAX_LENGTH = 1000; //maxLength on the database
  readonly RETURN_NUMBER_MAX_LENGTH = 50; //maxLength on the database
  readonly returnInfoWidgetTitle = 'return-information.label';
  readonly returnInfoWidgetSubtitle = 'return-information.label';

  @ViewChild('stepper') stepper: MatHorizontalStepper;
  @ViewChild('productTable') productTable: Table;
  @ViewChild('unitNumberInput') unitNumberInput: ElementRef<HTMLInputElement>;
  @ViewChild('productCodeInput') productCodeInput: ElementRef<HTMLInputElement>;
  @ViewChild('returnInfoForm') returnInfoForm: NgForm;
  @ViewChild('productSelectionForm') productSelectionForm: NgForm;

  returnOrderGroup: FormGroup;
  productGroup: FormGroup;
  commentForm: FormGroup;
  transitTimeRequest: TransitTimeRequestDto;
  transitTimeColor: string;
  transitResponseStatusKey: string;
  returnReasons: ReasonDto[] = [];
  productCategories: LookUpDto[] = [];
  timeZones: LookUpDto[] = [];
  inspections: LookUpDto[] = [];
  showExtraFields = false;
  showTransitTime = false;
  rareDonorExists = false;
  loading = false;
  productSelectionStep: boolean;
  returnInformation: Description[] = [];
  selectedSignOfTemperature: '+' | '-' = '+';
  products: ReturnItem[] = [];
  consequences: ReturnItemConsequence[] = [];
  selectedLabelingProductCategory: LookUpDto;

  constructor(
    public header: ProcessHeaderService,
    protected fb: FormBuilder,
    private inventoryService: InventoryService,
    private shipmentService: ShipmentService,
    private toaster: ToastrService,
    private translateService: TranslateService,
    private facilityService: FacilityService,
    private _router: Router,
    private route: ActivatedRoute,
    private cdr: ChangeDetectorRef
  ) {
    this.returnOrderGroup = fb.group({
      returnNumber: [null, Validators.maxLength(this.RETURN_NUMBER_MAX_LENGTH)],
      returnReason: [null, Validators.required],
      labelingProductCategoryValue: [null, Validators.required],
      inspection: [null, Validators.required],
    });

    this.productGroup = fb.group({
      unitNumber: [null, [Validators.required, RsaValidators.unitNumber]],
      productCode: [null, [Validators.required, RsaValidators.fullProductCode]],
    });

    this.commentForm = fb.group({
      comments: [null, Validators.maxLength(this.RETURN_COMMENTS_MAX_LENGTH)],
    });
  }

  ngOnInit(): void {
    if (this.route.snapshot?.data?.returnData) {
      const returnData = this.route.snapshot.data.returnData;
      this.returnReasons = returnData.reason;
      this.productCategories = returnData.lookups.filter(lookUp => lookUp.type === ORDER_PRODUCT_CATEGORY);
      this.timeZones = returnData.lookups.filter(lookUp => lookUp.type === RETURNS_TRANSIT_TIME_ZONE);
      this.inspections = returnData.lookups.filter(lookUp => lookUp.type === RETURNS_INSPECTION_STATUS);
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

  resetProcess() {
    this.resetReturnGroupForm();
    this.resetProductGroupForm();
    this.commentForm.reset();
    this.productTable.reset();
    this.stepper.reset();
    this.consequences = [];
    this.products = [];
    this.returnInformation = [];
    this.selectedSignOfTemperature = '+';
    this.transitTimeRequest = null;
    this.transitTimeColor = null;
    this.transitResponseStatusKey = null;
    this.productSelectionStep = false;
    this.rareDonorExists = false;
    this.loading = false;
    this.showExtraFields = false;
    this.showTransitTime = false;
    this.selectedLabelingProductCategory = null;
  }

  private showToaster(notification = { message: 'something-went-wrong.label', notificationType: 'error' }) {
    this.toaster.show(
      this.translateService.instant(notification.message),
      startCase(notification.notificationType),
      {},
      notification.notificationType
    );
  }

  private getLookUpDescriptionKey(optionValue: string, list: LookUpDto[]): string {
    return list.find(l => l.optionValue === optionValue)?.descriptionKey ?? '';
  }

  //#region STEP 1

  step1NextClick() {
    if (this.returnOrderGroup.valid) {
      this.inventoryService.validate(this.returnInfoValidateDto).subscribe(
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

            this.updateReturnInfoWidget(inspectionFailed);
            this.unitNumberInput.nativeElement.focus();
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
      this.products = [];
      this.rareDonorExists = false;
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
      this.returnOrderGroup.addControl('temperature', new FormControl('', Validators.required));

      if (categoryOptionValue === ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE) {
        this.showTransitTime = true;
        this.returnOrderGroup.addControl('transitTime', new FormControl('', Validators.required));
      }
    }
  }

  removeExtraFields() {
    this.returnOrderGroup.removeControl('temperature');
    this.returnOrderGroup.removeControl('transitTime');
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
    this.returnOrderGroup.patchValue({ transitTime: event?.transitTime });
  }

  onToggleTemperature(signal) {
    this.selectedSignOfTemperature = signal;
  }

  get returnInfoValidateDto() {
    return <ValidateRuleDto>{
      ruleName: RETURN_INFORMATION_VALIDATION_RULE,
      productCategory: this.returnOrderGroup.get('labelingProductCategoryValue').value.descriptionKey,
      returnReasonKey: this.returnOrderGroup.get('returnReason').value,
      temperature: this.returnOrderGroup.get('temperature')
        ? +`${this.selectedSignOfTemperature}${this.returnOrderGroup.get('temperature').value}`
        : '',
      visualInspectKey: this.returnOrderGroup.get('inspection').value,
      isImport: false,
      ...this.transitTimeRequest,
    };
  }

  private resetReturnGroupForm() {
    this.removeExtraFields();
    this.returnInfoForm.resetForm();
  }

  //#endregion

  //#region STEP 2

  onClickSubmit() {
    if (this.returnOrderGroup.valid && this.products.length) {
      this.shipmentService.confirmReturn(this.returnDto).subscribe(
        () => {
          this.toaster.success('return-process-complete.label');
          this.resetProcess();
        },
        error => {
          this.toaster.error('something-went-wrong.label');
          this.unitNumberInput.nativeElement.focus();
          throw error;
        }
      );
    }
  }

  onUnitNumberKeyOrTab(event: Event) {
    event.preventDefault();
    event.stopPropagation();
    this.productCodeInput.nativeElement.focus();
  }

  onProductCodeKeyOrTab() {
    if (this.productGroup.valid) {
      this.unitNumberInput.nativeElement.focus();

      if (!this.hasDuplicateProduct) {
        this.loading = true;

        this.inventoryService
          .validate(this.addProductValidationDto)
          .pipe(
            finalize(() => {
              this.resetProductGroupForm();
            })
          )
          .subscribe(
            response => {
              const value = response.body;

              if (value.ruleCode !== 'BAD_REQUEST') {
                for (const notification of value.notifications) {
                  this.showToaster(notification);
                }

                if (value.results?.returnItem?.length) {
                  const result = value.results?.returnItem[0] as ReturnItem;

                  if (result.rareDonor) {
                    this.rareDonorExists = true;
                  }

                  this.setProducts([...this.products, result]);
                }
              } else {
                this.showToaster();
              }
            },
            err => {
              this.showToaster();
              throw err;
            }
          );
      } else {
        this.resetProductGroupForm();
      }
    }
  }

  removeProd(index) {
    this.products.splice(index, 1);
    if (!this.products.some(prod => prod.rareDonor)) {
      this.rareDonorExists = false;
    }
    this.setProducts(this.products);
  }

  removeAll() {
    this.rareDonorExists = false;
    this.setProducts([]);
  }

  step2BackClick() {
    this.resetProductGroupForm();
    this.stepper.previous();
  }

  get hasDuplicateProduct() {
    const { unitNumber, productCode } = this.productGroup.value;
    return this.products.some(prod => prod.unitNumber === unitNumber && prod.isbtProductCode === productCode);
  }

  get quarantineExists() {
    return this.products?.length && (this.consequences?.length || this.products.some(prod => prod.quarantined));
  }

  get addProductValidationDto() {
    const prodFormValues = this.productGroup.value;
    return <ValidateRuleDto>{
      ruleName: ADD_PRODUCT_TO_BATCH_VALIDATION_RULE,
      unitNumber: prodFormValues.unitNumber,
      isbtProductCode: this.extractedProductCode,
      familyCategory: this.returnOrderGroup.get('labelingProductCategoryValue').value.optionValue,
    };
  }

  get returnDto() {
    const formValues = this.returnOrderGroup.value;
    return <ReturnsDto>{
      locationId: this.facilityService.getFacilityId(),
      temperature: formValues.temperature ? `${this.selectedSignOfTemperature}${formValues.temperature}` : null,
      comments: this.commentForm?.controls?.comments?.value ?? null,
      productCategory: formValues.labelingProductCategoryValue.descriptionKey,
      returnNumber: formValues.returnNumber ?? null,
      returnReasonKey: formValues.returnReason,
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
      transitTimeZone:
        this.timeZones?.find(timezone => timezone.optionValue === this.facilityService.getFacilityProperty('TZ'))
          ?.optionValue ?? null,
      transitTimeResultKey: this.transitResponseStatusKey ?? null,
      returnsItems: this.products?.map(prod => {
        return <ReturnsItemDto>{
          inventoryId: prod.inventoryId,
          productCode: prod.isbtProductCode,
          unitNumber: prod.unitNumber,
          productConsequenceKey:
            this.consequences?.length || prod.returnItemConsequences?.length
              ? QUARANTINE_CONSEQUENCE_TYPE
              : RETURN_TO_INVENTORY_CONSEQUENCE_TYPE,
          returnsItemConsequences: [
            ...[...this.consequences, ...(prod.returnItemConsequences ?? [])].map(con => {
              return <ReturnsItemConsequenceDto>{
                itemConsequenceReasonKey: con.consequenceReasonKey,
                itemConsequenceType: con.consequenceType,
              };
            }),
          ],
        };
      }),
    };
  }

  private setProducts(products: ReturnItem[]) {
    this.products = products;
    this.productTable?.reset();
  }

  private updateReturnInfoWidget(inspectionFailed: boolean) {
    const formValues = this.returnOrderGroup.value;
    this.returnInformation = [
      { label: 'return-number.label', value: formValues.returnNumber },
      {
        label: 'return-reason.label',
        value: formValues.returnReason,
      },
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
        this.returnInformation.splice(3, 0, {
          label: 'transit-time.label',
          value: formValues.transitTime,
          valueStyle: { color: this.transitTimeColor },
        });
      }
      this.returnInformation.push({
        label: 'temperature.label',
        value: `${this.selectedSignOfTemperature}${formValues.temperature}${this.translateService.instant(
          'celsius.label'
        )}`,
      });
    }
  }

  private resetProductGroupForm() {
    this.loading = false;

    this.productSelectionForm.resetForm();
    this.productGroup.controls.unitNumber.setErrors(null);
    this.productGroup.controls.productCode.setErrors(null);
  }

  get extractedProductCode() {
    const productCode = this.productGroup.value.productCode;
    if (new RegExp(commonRegex.scannedProductCode).test(productCode)) {
      return productCode.replace(new RegExp(commonRegex.extractProductCode), (match, g1, g2, g3, g4) => g2 + g3 + g4);
    }
    return productCode;
  }

  //#endregion
}
