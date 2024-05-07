import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { formatDate } from '@angular/common';
import { Component, Inject, LOCALE_ID, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatHorizontalStepper } from '@angular/material/stepper';
import { ActivatedRoute, Router } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import {
  ConfirmDialogComponent,
  CustomerDto,
  Description,
  Facility,
  FacilityService,
  InventoryService,
  LookUpDto,
  OrderDto,
  OrderService,
  ProcessHeaderService,
  RulesService,
  ShipmentDto,
  ShipmentService,
  TransferReceiptDto,
  TransferReceiptService,
  TransitTimeRequestDto,
  ValidateRuleDto,
  ValidationType,
} from '@rsa/commons';
import { ProductSelectionItemModel } from '@rsa/distribution/core/models/internal-transfers.model';
import {
  LabelingProductCategoryType,
  ORDER_PRODUCT_CATEGORY_FROZEN,
  ORDER_PRODUCT_CATEGORY_REFRIGERATED,
  ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE,
} from '@rsa/distribution/core/models/orders.model';
import {
  QUARANTINE_CONSEQUENCE_TYPE,
  RESULT_PROPERTY_INSPECTION_FIELD,
  ReturnItemConsequence,
  RETURNS_INSPECTION_STATUS,
  RETURNS_TRANSIT_TIME_ZONE,
} from '@rsa/distribution/core/models/returns.models';
import {
  ORDER_PRODUCT_CATEGORY,
  TRANSFER_RECEIPT_ELIGIBILITY_RULES_NAME,
  TRANSFER_RECEIPT_VALIDATION_NAME,
} from '@rsa/distribution/core/models/transfer-receipt.model';
import { ToastrService } from 'ngx-toastr';
import { Table } from 'primeng/table';
import { forkJoin, Observable } from 'rxjs';
import { catchError, map, switchMap } from 'rxjs/operators';

@Component({
  selector: 'rsa-transfer-receipt',
  templateUrl: './transfer-receipt.component.html',
  styleUrls: ['./transfer-receipt.component.scss'],
})
export class TransferReceiptComponent implements OnInit {
  constructor(
    private _router: Router,
    private matDialog: MatDialog,
    private orderService: OrderService,
    private inventoryService: InventoryService,
    private facilityService: FacilityService,
    private translateService: TranslateService,
    private rulesService: RulesService,
    private toaster: ToastrService,
    private route: ActivatedRoute,
    public header: ProcessHeaderService,
    protected fb: FormBuilder,
    private transferReceiptService: TransferReceiptService,
    private shipmentService: ShipmentService,
    @Inject(LOCALE_ID) public locale: string
  ) {
    this.transferInfoGroup = fb.group({
      transferOrderNumber: ['', [Validators.required, Validators.maxLength(this.ORDER_NUMBER_MAX_LENGTH)]],
      inspection: ['', Validators.required],
    });
  }

  get isLabelingProdCategoryRoomTemperature() {
    return this.labelingProductCategory === ORDER_PRODUCT_CATEGORY_ROOM_TEMPERATURE;
  }

  get isLabelingProdCategoryFrozen() {
    return this.labelingProductCategory === ORDER_PRODUCT_CATEGORY_FROZEN;
  }

  get isLabelingProdCategoryRefrigerated() {
    return this.labelingProductCategory === ORDER_PRODUCT_CATEGORY_REFRIGERATED;
  }

  get getLabelingProductCategoryDescription() {
    return this.labelingProdCategories?.find(l => l.optionValue === this.labelingProductCategory)?.descriptionKey;
  }

  readonly validationType = ValidationType;
  readonly ORDER_NUMBER_MAX_LENGTH = 50; //maxLength on the database

  @ViewChild('stepper') stepper: MatHorizontalStepper;
  @ViewChild('productTable') productTable: Table;

  transferInfoGroup: FormGroup;
  productSelectionGroup: FormGroup;
  productSelectionStep: boolean;
  products: ProductSelectionItemModel[] = [];
  transitTimeRequest: TransitTimeRequestDto;
  transitTimeColor: string;
  transitResponseStatusKey: string;

  unitNumberFocus = false;
  customers: Observable<CustomerDto[]>;
  selectedCustomer: CustomerDto;
  readonly maxChars = 500;
  commentControl = new FormControl('');
  timezones: LookUpDto[];
  inspections: LookUpDto[];
  labelingProdCategories: LookUpDto[];
  currentOrder: OrderDto;
  labelingProductCategory: LabelingProductCategoryType;
  transferInformation: Description[] = [];
  focusTransferOrderNumber = true;
  enableCompleteButton = false;
  consequences: ReturnItemConsequence[] = [];

  completeTransferPayload?: Partial<TransferReceiptDto>;
  markForQuarantine = false;

  ngOnInit(): void {
    const transferReceiptData = this.route.snapshot?.data?.transferReceipt;
    if (transferReceiptData) {
      this.timezones = transferReceiptData.lookups.filter(lookUp => lookUp.type === RETURNS_TRANSIT_TIME_ZONE);
      this.inspections = transferReceiptData.lookups.filter(lookUp => lookUp.type === RETURNS_INSPECTION_STATUS);
      this.labelingProdCategories = transferReceiptData.lookups.filter(
        lookUp => lookUp.type === ORDER_PRODUCT_CATEGORY
      );
    }
    this.updateTransferInfoGroupControls();
  }

  private updateTransferInfoGroupControls() {
    if (this.isLabelingProdCategoryRefrigerated || this.isLabelingProdCategoryRoomTemperature) {
      this.transferInfoGroup.addControl('temperature', new FormControl('', Validators.required));

      if (this.isLabelingProdCategoryRoomTemperature) {
        this.transferInfoGroup.addControl('transitTime', new FormControl('', Validators.required));
      } else {
        this.transferInfoGroup.removeControl('transitTime');
      }
    } else {
      this.transferInfoGroup.removeControl('temperature');
      this.transferInfoGroup.removeControl('transitTime');
    }
  }

  stepSelectionChange($event: StepperSelectionEvent) {
    this.productSelectionStep = $event.selectedIndex === 1;
    if (this.productSelectionStep) {
      this.unitNumberFocus = true;
    }
  }

  step1NextClick() {
    forkJoin({
      validate: this.inventoryService.validate(this.getValidateParam()).pipe(map(reasonRes => reasonRes?.body)),
      facility: this.facilityService.getFacilityById(this.currentOrder.locationId).pipe(map(response => response.body)),
    }).subscribe(
      ({ validate, facility }) => {
        this.consequences =
          validate.results?.consequences?.filter(con => con.consequenceType === QUARANTINE_CONSEQUENCE_TYPE) ?? [];
        const inspectionFailed =
          this.consequences?.some(con => con.resultProperty === RESULT_PROPERTY_INSPECTION_FIELD) ?? false;
        this.updateTransferInfoWidget(facility, inspectionFailed);
        this.stepper.next();
        const notificationWarning = validate.notifications.find(n => n.notificationType === 'warning');
        if (notificationWarning) {
          this.toaster.error(notificationWarning.message);
        }
      },
      err => {
        this.toaster.error('something-went-wrong.label');
        throw err;
      }
    );
  }

  private getValidateParam(): ValidateRuleDto {
    return {
      ruleName: TRANSFER_RECEIPT_VALIDATION_NAME,
      productCategory: this.getLabelingProductCategoryDescription,
      temperature: this.transferInfoGroup.get('temperature')?.value
        ? +`${this.transferInfoGroup.get('temperature')?.value}`
        : '',
      visualInspectKey: this.transferInfoGroup.get('inspection').value,
      ...this.transitTimeRequest,
    };
  }

  step2BackClick() {
    this.transferInfoGroup.get('temperature')?.reset();
    this.transferInfoGroup.reset();
    this.commentControl.reset(null);
    this.transitTimeRequest = null;
    this.consequences = [];
    this.transitResponseStatusKey = null;
    this.labelingProductCategory = null;
  }

  private updateTransferInfoWidget(facility: Facility, inspectionFailed?: boolean) {
    const formValues = this.transferInfoGroup.value;
    this.transferInformation = [
      { label: 'transfer-number.label', value: formValues.transferOrderNumber },
      {
        label: 'date-transferred.label',
        value: formatDate(this.currentOrder.desireShippingDate, 'MM/dd/YYYY', this.locale),
      },
      {
        label: 'labeling-product-category.label',
        value: this.labelingProdCategories.find(
          category => category.optionValue === this.currentOrder?.productCategoryKey
        )?.descriptionKey,
      },
      {
        label: 'inspection.label',
        value: [this.inspections.find(inspection => inspection.optionValue === formValues.inspection)?.descriptionKey],
        valueType: 'badge',
        valueCls: 'badge break-word',
        valueStyle: [
          {
            'background-color': inspectionFailed ? '#f65151' : '#4caf50',
            color: 'white',
          },
        ],
      },
    ];

    if (!this.isLabelingProdCategoryFrozen) {
      if (this.isLabelingProdCategoryRoomTemperature) {
        this.transferInformation.splice(
          3,
          0,
          {
            label: 'transit-time.label',
            value: this.handleCustomTransitTime(formValues.transitTime),
            valueStyle: { color: '#000' },
          },
          {
            label: 'transit-time-result.label',
            value: this.transitTimeColor === 'red' ? ['unacceptable.label'] : ['acceptable.label'],
            valueType: 'badge',
            valueCls: 'badge',
            valueStyle: [
              {
                'background-color': this.transitTimeColor === 'red' ? '#f65151' : '#4caf50',
                color: 'white',
              },
            ],
          }
        );
      }
      this.transferInformation.push({
        label: 'temperature.label',
        value: `${formValues.temperature}${this.translateService.instant('celsius.label')}`,
      });
    }
    this.transferInformation.push(
      { label: 'label-status.label', value: this.currentOrder?.labelStatus },
      { label: 'originating-facility.label', value: facility.name }
    );
  }

  handleCustomTransitTime(transitTime: string): string {
    return transitTime.replace(' and ', ' ').replace(/^0+/, '').replace('minutes', 'mins');
  }

  cancel() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '32rem';
    const dialogRef = this.matDialog.open(ConfirmDialogComponent, dialogConfig);
    dialogRef.componentInstance.confirmMessage = 'are-you-sure-want-cancel-message.label';
    dialogRef.componentInstance.title = 'confirmation.label';
    dialogRef.componentInstance.acceptTitle = 'confirm.label';
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this._router.navigateByUrl('/home');
      }
    });
  }

  transferOrderNumberBlur($event: FocusEvent) {
    const value = ($event?.target as HTMLInputElement)?.value;
    if (value) {
      this.rulesService
        .evaluation(this.getEvaluationParam(value))
        .pipe(
          catchError(err => {
            this.toaster.error('something-went-wrong.label');
            throw err;
          }),
          map(response => response.body),
          map(evaluation => {
            const notificationError = evaluation.notifications.find(
              n => n.statusCode === '400' && n.notificationType === 'error'
            );
            if (notificationError) {
              this.toaster.error(notificationError.message);
              this.transferInfoGroup.get('transferOrderNumber').reset();
              throw new Error();
            }
          }),
          switchMap(() => this.orderService.getOrderByCriteria({ 'orderNumber.equals': value })),
          map(response => response.body),
          map(order => {
            this.currentOrder = order[0];
            if (this.currentOrder?.orderNumber) {
              this.labelingProductCategory =
                (this.currentOrder?.productCategoryKey as LabelingProductCategoryType) ||
                'ORDER_PRODUCT_CATEGORY_FROZEN';
              this.updateTransferInfoGroupControls();
            } else {
              this.toaster.error('the-order-number-not-exist.message');
              this.transferInfoGroup.get('transferOrderNumber').reset();
            }
          })
        )
        .subscribe();
    }
  }

  private getEvaluationParam(orderNumber) {
    return {
      ruleName: TRANSFER_RECEIPT_ELIGIBILITY_RULES_NAME,
      ruleInputs: {
        orderNumber: orderNumber,
        currentLocationId: this.facilityService.getFacilityId(),
      },
    };
  }

  updateTransitTime(event: {
    transitTimeRequest: TransitTimeRequestDto;
    transitTime: string;
    transitResponseStatusKey: string;
    transitTimeColor: string;
  }) {
    this.transitTimeRequest = event?.transitTimeRequest;
    this.transitTimeColor = event?.transitTimeColor;
    this.transferInfoGroup.patchValue({ transitTime: event?.transitTime });
    this.transitResponseStatusKey = event?.transitResponseStatusKey;
  }

  handleProductSelectionChange(event: { products: ProductSelectionItemModel[]; markForQuarantine?: boolean }) {
    this.products = event.products;
    this.markForQuarantine = false;
    if (this.products.find(p => p.quarantine) || event.markForQuarantine) {
      this.markForQuarantine = true;
    }
    this.enableCompleteButton = event.products.length > 0 && this.commentControl.valid && this.transferInfoGroup.valid;

    if (this.enableCompleteButton) {
      this.completeTransferPayload = {
        orderId: this.currentOrder.id,
        orderNumber: this.currentOrder.orderNumber,
        transferReceiptItems: event.products.map(item => {
          return {
            inventoryId: item.id,
            unitNumber: item.unitNumber,
            productCode: item.productCode,
            transferReceiptItemConsequences: [
              ...this.consequences.map(con => {
                return {
                  itemConsequenceReasonKey: con.consequenceReasonKey,
                  itemConsequenceType: con.consequenceType,
                };
              }),
            ],
          };
        }),
        locationId: this.facilityService.getFacilityId(),
        temperature: this.transferInfoGroup.get('temperature')?.value,
        totalTransitTime: this.transferInfoGroup.get('transitTime')?.value,
        transitStartDateTime: this.transitStartDateTime(),
        transitTimeZone: this.transitTimeRequest?.transitEndTimeZone,
        transitEndDateTime: this.transitEndDateTime(),
        transitTimeResultKey: this.transitResponseStatusKey ?? null,
        shipmentInspectKey: this.transferInfoGroup.get('inspection')?.value,
        comments: this.commentControl.value,
      };
    } else {
      this.completeTransferPayload = undefined;
    }
  }

  transitStartDateTime() {
    if (this.transitTimeRequest?.transitStartDate && this.transitTimeRequest?.transitStartTime)
      return new Date(
        `${this.transitTimeRequest?.transitStartDate} ${this.transitTimeRequest?.transitStartTime}`
      ).toISOString();
    return null;
  }

  transitEndDateTime() {
    if (this.transitTimeRequest?.transitEndDate && this.transitTimeRequest?.transitEndTime)
      return new Date(
        `${this.transitTimeRequest?.transitEndDate} ${this.transitTimeRequest?.transitEndTime}`
      ).toISOString();
    return null;
  }

  private isAllProductsEntered(shipments: ShipmentDto[], transferReceipts: TransferReceiptDto[]) {
    if (shipments == null) return false;

    const productsShippedCount = shipments.reduce((acc, shipment) => acc + shipment.shipmentItems?.length, 0);
    const transferReceiptItemsCount =
      transferReceipts?.reduce((acc, transferReceipt) => acc + transferReceipt.transferReceiptItems?.length, 0) || 0;

    return this.products.length + transferReceiptItemsCount >= productsShippedCount;
  }

  onComplete() {
    forkJoin({
      shipments: this.shipmentService
        .getShipmentByCriteria({ orderId: this.currentOrder.id })
        .pipe(map(response => response.body)),
      transferReceipts: this.transferReceiptService
        .getTransferReceiptsByCriteria({ orderId: this.currentOrder.id })
        .pipe(map(response => response.body)),
    }).subscribe(
      ({ shipments, transferReceipts }) => {
        if (!this.isAllProductsEntered(shipments, transferReceipts)) {
          const dialogConfig = new MatDialogConfig();
          const dialogRef = this.matDialog.open(ConfirmDialogComponent, dialogConfig);
          dialogRef.componentInstance.enableComment = true;
          dialogRef.componentInstance.title = 'confirmation.label';
          dialogRef.componentInstance.confirmMessage = 'all-products-have-not-been-entered.message';
          dialogRef.componentInstance.acceptTitle = 'continue.label';
          dialogRef.afterClosed().subscribe(result => {
            if (result) {
              this.completeTransferPayload.comments = result.comment;
              this.createTransferReceipt();
            }
          });
        } else {
          this.createTransferReceipt();
        }
      },
      err => {
        this.toaster.error('something-went-wrong.label');
        throw err;
      }
    );
  }

  createTransferReceipt() {
    this.transferReceiptService.createTransferReceipt(this.completeTransferPayload).subscribe(response => {
      if (response.status === 201) {
        if (this.isAnyQuarantineApplied(response.body) || this.markForQuarantine) {
          this.toaster.warning('transfer-receipt-products-quarantined.label');
        }
        this.toaster.success('transfer-complete.message', 'success.label');
        this.step2BackClick();
        this.stepper.reset();
      } else {
        this.toaster.error('something-went-wrong.label', 'error.label');
      }
    });
  }

  isAnyQuarantineApplied(transferReceipt: TransferReceiptDto): boolean {
    let hasQuarantine = false;

    transferReceipt.transferReceiptItems.forEach(transferReceiptItem => {
      transferReceiptItem.transferReceiptItemConsequences.forEach(transferReceiptItemConsequence => {
        if (QUARANTINE_CONSEQUENCE_TYPE === transferReceiptItemConsequence.itemConsequenceType) {
          hasQuarantine = true;
        }
      });
    });

    return hasQuarantine;
  }
}
