import { StepperSelectionEvent } from '@angular/cdk/stepper';
import { HttpResponse } from '@angular/common/http';
import { Component, OnDestroy, OnInit, Renderer2, ViewChild } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatDatepickerInputEvent } from '@angular/material/datepicker';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { MatHorizontalStepper } from '@angular/material/stepper';
import { ActivatedRoute, Router } from '@angular/router';
import {
  ConfirmDialogComponent,
  CustomerDto,
  CustomerService,
  Description,
  FacilityService,
  LocationDto,
  LocationService,
  LookUpDto,
  Option,
  OrderBloodTypeDto,
  OrderDto,
  OrderItemDto,
  OrderItemProductAttributeDto,
  OrderProductAttributeDto,
  OrderProductFamilyDto,
  OrderService,
  ProcessHeaderService,
  ProcessProductDto,
  RuleResponseDto,
  RulesService,
  ValidationType,
} from '@rsa/commons';
import {
  ANTIGEN_TESTED,
  BloodTypeAndQuantity,
  DELIVERY_TYPE_DEFAULT_VALUE,
  OPEN_OPTION_VALUE,
  OrderModel,
  OrderProduct,
  ORDER_BILLING_CUSTOMER,
  ORDER_DELIVERY_TYPE,
  ORDER_LABELED_OPTION,
  ORDER_LABEL_STATUS,
  ORDER_OUT_OF_STATE_RULE_NAME,
  ORDER_PRODUCT_CATEGORY,
  ORDER_SERVICE_FEE,
  ORDER_SHIPMENT_TYPE,
  ORDER_SHIPPING_CUSTOMER,
  ORDER_SHIPPING_METHOD,
  ORDER_STATUS,
  ORDER_UNLABELED_OPTION,
  ShipmentType,
  SHIPPING_METHOD_DEFAULT_VALUE,
} from '@rsa/distribution/core/models/orders.model';
import { SortService } from '@rsa/distribution/core/services/sort.service';
import { AddProductModalComponent } from '@rsa/distribution/modules/orders/add-product-modal/add-product-modal.component';
import { ServiceFeeModalComponent } from '@rsa/distribution/modules/orders/service-fee-modal/service-fee-modal.component';
import { rowExpansionTrigger } from '@rsa/distribution/shared/animations/row-expansion-trigger';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { SortEvent } from 'primeng/api';
import { Table } from 'primeng/table';
import { Observable, of, Subject } from 'rxjs';
import { catchError, debounceTime, distinctUntilChanged, map, switchMap, takeUntil, tap } from 'rxjs/operators';

@Component({
  selector: 'rsa-create-order',
  templateUrl: './create-order.component.html',
  styleUrls: ['./create-order.component.scss'],
  animations: [rowExpansionTrigger],
})
export class CreateOrderComponent implements OnInit, OnDestroy {
  readonly validationType = ValidationType;
  readonly EXTERNAL_ID_MAX_LENGTH = 50;
  readonly ORDER_COMMENT_MAX_LENGTH = 1000;
  readonly BILLING_CUSTOMER = ORDER_BILLING_CUSTOMER;
  readonly SHIPPING_CUSTOMER = ORDER_SHIPPING_CUSTOMER;

  @ViewChild('stepper') stepper: MatHorizontalStepper;
  @ViewChild('productTable') productTable: Table;

  orderGroup: FormGroup;
  productCountControl: FormControl;
  shippingMethods: LookUpDto[] = [];
  priorities: LookUpDto[] = [];
  labelingProdCategories: LookUpDto[] = [];
  serviceFees: LookUpDto[] = [];
  shipmentTypes: LookUpDto[] = [];
  products: OrderProduct[] = [];
  reviewAndSubmitStep: boolean;
  productSelectionStep: boolean;
  orderInfoDescriptions: Description[] = [];
  billInfoDescriptions: Description[] = [];
  shippingInfoDescriptions: Description[] = [];
  feesInfoDescriptions: Description[] = [];
  prodSelectionDescriptions: Description[] = [];
  shipCustomers: Observable<CustomerDto[]>;
  billCustomers: Observable<CustomerDto[]>;
  selectedShipCustomer: CustomerDto;
  selectedBillCustomer: CustomerDto;
  showPastOrderWarning = false;
  customers: { BILLING: CustomerDto[]; SHIPPING: CustomerDto[] } = {
    BILLING: [],
    SHIPPING: [],
  };
  processProductConfig: ProcessProductDto;
  currentOrder: OrderModel;
  lockedLabelingProductCategoryValue: string;
  lockedLabelStatusValue: string;
  statuses: LookUpDto[] = [];
  bloodTypes: OrderBloodTypeDto[] = [];
  productAttributes: OrderProductAttributeDto[] = [];

  labelStatuses: LookUpDto[] = [];
  filteredLocations$: Observable<LocationDto[]>;

  private selectShipCustomerTriggered = false;
  private selectBillCustomerTriggered = false;
  private unsubscribeAll = new Subject();

  constructor(
    private _router: Router,
    private route: ActivatedRoute,
    public header: ProcessHeaderService,
    protected fb: FormBuilder,
    private matDialog: MatDialog,
    private orderService: OrderService,
    private toaster: ToastrService,
    private customerService: CustomerService,
    private facilityService: FacilityService,
    private sortService: SortService,
    private locationService: LocationService,
    private rulesService: RulesService,
    private renderer: Renderer2
  ) {
    this.orderGroup = fb.group({
      orderNumber: [{ value: '', disabled: true }],
      externalId: ['', Validators.maxLength(this.EXTERNAL_ID_MAX_LENGTH)],
      shipmentType: [ShipmentType.CUSTOMER, Validators.required],
      desireShippingDate: [new Date(), Validators.required],
      shippingMethod: ['', Validators.required],
      deliveryType: ['', Validators.required],
      sameCustomer: [''],
      comments: ['', Validators.maxLength(this.ORDER_COMMENT_MAX_LENGTH)],
      productCategoryKey: ['', Validators.required],
    });

    this.productCountControl = fb.control(0, Validators.min(1));
  }

  ngOnInit(): void {
    const orderConfigData = this.route.snapshot.data?.createOrderConfigData as HttpResponse<
      | LookUpDto[]
      | OrderBloodTypeDto[]
      | OrderProductAttributeDto[]
      | ProcessProductDto
      | OrderDto
      | { orderNumber: string }
    >[];

    if (orderConfigData?.length) {
      this.loadLookupFields(orderConfigData[0]?.body as LookUpDto[]);
      this.loadProdAttributes(orderConfigData[1]?.body as OrderProductAttributeDto[]);
      this.loadIconConfig(orderConfigData[2]?.body as ProcessProductDto);

      this.orderGroup.controls['shippingMethod'].setValue(
        this.shippingMethods.find(x => x.optionValue.trim().toUpperCase() === SHIPPING_METHOD_DEFAULT_VALUE)
          ?.optionValue ?? ''
      );
      this.orderGroup.controls['deliveryType'].setValue(
        this.priorities.find(x => x.optionValue.trim().toUpperCase() === DELIVERY_TYPE_DEFAULT_VALUE)?.optionValue ?? ''
      );

      const orderId = this.route.snapshot.params.id;
      // EDit order
      if (orderId) {
        this.bloodTypes = orderConfigData[3]?.body as OrderBloodTypeDto[];
        this.loadCustomers(orderConfigData[4]?.body as OrderDto);
        this.handleFieldsEditOrder();
      } else {
        this.loadNextOrderNumber((orderConfigData[3]?.body as { orderNumber: string })?.orderNumber);
      }
    }

    // Add or remove controls depending on shipment type
    this.addRemoveControlShipmentType(ShipmentType.CUSTOMER);

    this.setProductCount();
  }

  private handleFieldsEditOrder() {
    this.orderGroup.get('shipmentType').disable();
    if (this.internalTransferSelected) {
      this.orderGroup.get('shipToLocation').disable();
    }
  }

  stepSelectionChange($event: StepperSelectionEvent) {
    this.reviewAndSubmitStep = $event.selectedIndex === 2;
    this.productSelectionStep = $event.selectedIndex === 1;
    if (this.reviewAndSubmitStep) {
      this.fillReviewAndSubmit();
    } else if (this.productSelectionStep) {
      this.setProductSelectionInfo();
    }
  }

  step1NextClick() {
    if (this.orderGroup.value.externalId) {
      const criteria = this.currentOrder ? { 'id.notIn': this.currentOrder.id } : {};
      this.orderService.getOrderByCriteria({ ...criteria, externalId: this.orderGroup.value.externalId }).subscribe(
        orderRes => {
          if (orderRes?.body?.length) {
            this.orderGroup.get('externalId').setErrors({ alreadyExists: true });
          } else {
            this.validateCustomers();
          }
        },
        err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        }
      );
    } else {
      this.validateCustomers();
    }
  }

  step2NextClick() {
    this.stepper.next();
    //forcing table refresh
    this.products = [...this.products];
  }

  isAutoCompleteSelected(): boolean {
    return this.internalTransferSelected
      ? this.orderGroup.controls.shipToLocation?.value?.id
      : this.orderGroup.controls.shipToCustomer?.value?.id && this.orderGroup.controls.billToCustomer?.value?.id;
  }

  getIcon(productFamily: string): string {
    return productFamily && this.processProductConfig?.properties[`icon.${productFamily}`]
      ? 'rsa:' + this.processProductConfig.properties[`icon.${productFamily}`]
      : 'rsa:product-whole-blood';
  }

  serviceFeeClick() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '60rem';
    dialogConfig.data = {
      addedFees: this.orderGroup.get('orderServiceFees').value,
      serviceFees: this.serviceFees,
    };
    const dialogRef = this.matDialog.open(ServiceFeeModalComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (Array.isArray(result)) {
        this.orderGroup.get('orderServiceFees').setValue(result);
      }
    });
  }

  addProduct() {
    this.openAddEditProduct();
  }

  addOrEditProduct(product: OrderProduct, isEdit: boolean) {
    if (isEdit) {
      this.products.splice(product.rowIndex, 1, product);
    } else {
      this.products.push({
        ...product,
        rowIndex: this.products.length,
      });
    }
    this.productTable.reset();
    this.lockedLabelingProductCategoryValue = this.orderGroup.value.productCategoryKey;
    this.lockedLabelStatusValue = this.orderGroup.value.labelStatus;
    this.setProductCount();
  }

  edit(product: OrderProduct) {
    this.openAddEditProduct(product);
  }

  delete(rowIndex: number) {
    this.products.splice(rowIndex, 1);
    this.productTable.reset();
  }

  cancelOrder() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '60rem';
    const dialogRef = this.matDialog.open<ConfirmDialogComponent>(ConfirmDialogComponent, dialogConfig);
    dialogRef.componentInstance.confirmMessage = this.currentOrder
      ? 'cancel-order-edit-form-warning-message.label'
      : 'cancel-order-form-warning-message.label';
    dialogRef.componentInstance.acceptTitle = 'continue.label';
    dialogRef.componentInstance.title = 'cancel-order.label';
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        if (this.currentOrder) {
          this.toaster.success('the-changes-have-been-cancelled.label');
          this._router.navigateByUrl(`orders/${this.currentOrder.id}/details`);
        } else this._router.navigateByUrl('/').then(() => this._router.navigateByUrl('/orders/create'));
      }
    });
  }

  onSubmit() {
    const orderDto = this.toOrderDto();

    let $sub;
    if (this.currentOrder) {
      $sub = this.orderService.editOrder(this.currentOrder.id, orderDto);
    } else {
      $sub = this.orderService.createOrder(orderDto);
    }

    $sub.subscribe(
      response => {
        this.toaster.success('the-order-has-been-submitted.label');
        this._router.navigateByUrl(`/orders/${response.id}/details`);
      },
      err => {
        this.toaster.error('something-went-wrong.label');
        throw err;
      }
    );
  }

  showHidePastWarning(event: MatDatepickerInputEvent<unknown, unknown | null>) {
    this.showPastOrderWarning = event.value && moment(event.value)?.isBefore(new Date(), 'day');
  }

  labelingProdSelectionChange($event) {
    if ($event.value !== this.lockedLabelingProductCategoryValue) {
      this.showCleanOrderProductsDialog();
    }
  }

  showCleanOrderProductsDialog(labelStatusChanged = false) {
    if (this.products.length > 0) {
      const dialogRef = this.matDialog.open<ConfirmDialogComponent>(ConfirmDialogComponent);
      dialogRef.componentInstance.confirmMessage = 'edit-labeling-product-category-warning-message.label';
      dialogRef.componentInstance.acceptTitle = 'continue.label';
      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.products = [];
          this.lockedLabelingProductCategoryValue = null;
          this.lockedLabelStatusValue = null;
        } else {
          this.orderGroup.patchValue({
            productCategoryKey: this.lockedLabelingProductCategoryValue,
            labelStatus: this.lockedLabelStatusValue,
          });
        }
        if (labelStatusChanged) {
          this.verifyProductUnlabeledOutState(this.orderGroup.get('shipToLocation').value);
        }
      });
    } else {
      if (labelStatusChanged) {
        this.verifyProductUnlabeledOutState(this.orderGroup.get('shipToLocation').value);
      }
    }
  }

  sortProducts(event: SortEvent) {
    this.sortService.customSort(event);
  }

  productQuantityChange(event, product: OrderProduct) {
    if (event && !isNaN(event) && +event > 0) {
      product.quantity = +event;
    }
  }

  get totalProducts(): number {
    return this.products.reduce<number>(
      (previousValue: number, currentValue: OrderProduct) => previousValue + +currentValue?.quantity,
      0
    );
  }

  get labelingProductCategory() {
    return (
      this.labelingProdCategories?.find(
        prodCat => prodCat.optionValue === this.orderGroup.get('productCategoryKey')?.value
      )?.descriptionKey ?? ''
    );
  }

  //#region Customer

  validateCustomers() {
    this.orderGroup.get('externalId').setErrors(null);
    if (
      this.internalTransferSelected ||
      [
        this.validateCustomerOrError(this.selectedShipCustomer, this.orderGroup.controls.shipToCustomer),
        this.validateCustomerOrError(this.selectedBillCustomer, this.orderGroup.controls.billToCustomer),
      ].every(Boolean)
    ) {
      this.stepper.next();
    }
  }

  filterCustomers(val: string, type: string): Observable<CustomerDto[]> {
    const criteria = this.getCustomerSearchCriteria(val, type);
    return this.customerService.getCustomerByCriteria({ ...criteria, active: 'true' }).pipe(
      map(response => {
        const controlType = type === ORDER_SHIPPING_CUSTOMER ? 'shipToCustomer' : 'billToCustomer';
        this.orderGroup.get(controlType).enable();
        this.renderer.selectRootElement(`#${controlType}`).focus();

        const customers = response.body ?? [];
        if (!customers.length) {
          this.orderGroup.get(controlType).setErrors({ noMatchingCustomer: true });
        } else {
          this.orderGroup.get(controlType).setErrors(null);
        }
        return customers;
      })
    );
  }

  selectedCustomer(value: CustomerDto, type: string): void {
    if (type === ORDER_BILLING_CUSTOMER) {
      this.selectedBillCustomer = value;
      this.selectBillCustomerTriggered = true;
    } else if (type === ORDER_SHIPPING_CUSTOMER) {
      this.selectedShipCustomer = value;
      this.selectShipCustomerTriggered = true;
    }
  }

  selectedLocation(location: LocationDto): void {
    this.verifyProductUnlabeledOutState(location);
  }

  private verifyProductUnlabeledOutState(location: LocationDto) {
    if (this.isUnlabeled() && this.orderGroup.controls?.shipToLocation) {
      this.rulesService
        .evaluation(this.getOutOfStateRuleParam(location))
        .pipe(
          map(response => response.body),
          map(evaluation => {
            if (this.isOutOfStateWarning(evaluation)) {
              this.openUnlabeledConfirmDialogOutState();
            }
          })
        )
        .subscribe();
    }
  }

  private getOutOfStateRuleParam(location: LocationDto) {
    return {
      ruleName: ORDER_OUT_OF_STATE_RULE_NAME,
      ruleInputs: {
        labelStatus: this.orderGroup.controls?.labelStatus?.value,
        locationId: this.facilityService.getFacilityId(),
        shipToLocationId: location?.id,
      },
    };
  }

  private isOutOfStateWarning(evaluation: RuleResponseDto) {
    return evaluation.notifications.find(
      n => n.notificationType === 'warning' && n.message === 'rul-warning-out-of-state.label'
    );
  }

  private isUnlabeled(): boolean {
    return this.orderGroup.controls?.labelStatus?.value === ORDER_UNLABELED_OPTION;
  }

  displayCustomerFn(customer: CustomerDto): string {
    return customer ? `${customer.externalId} - ${customer.name}` : '';
  }

  displayLocationFn(location: LocationDto): string {
    return location ? `${location.name}` : '';
  }

  private getCustomerFilter(control: AbstractControl, type: string): Observable<CustomerDto[]> {
    return control.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      tap(val => this.resetCustomer(control)),
      map(val => (typeof val === 'string' ? val : null)), // Added because of `displayFn`, which makes the `valueChanges` to be called again.
      switchMap(val => {
        if (
          val &&
          ((control === this.orderGroup.controls.shipToCustomer && !this.selectedShipCustomer) ||
            (control === this.orderGroup.controls.billToCustomer && !this.selectedBillCustomer))
        ) {
          this.orderGroup.get(type === ORDER_SHIPPING_CUSTOMER ? 'shipToCustomer' : 'billToCustomer').disable();
          return this.filterCustomers(val, type);
        }

        return of([]);
      })
    );
  }

  private resetCustomer(control: AbstractControl) {
    if (control === this.orderGroup.controls.shipToCustomer) {
      if (!this.selectShipCustomerTriggered) {
        this.selectedShipCustomer = null;
      }
      this.selectShipCustomerTriggered = false;
    } else if (control === this.orderGroup.controls.billToCustomer) {
      if (!this.selectBillCustomerTriggered) {
        this.selectedBillCustomer = null;
      }
      this.selectBillCustomerTriggered = false;
    }
  }

  private getCustomerSearchCriteria(val: String, type: string) {
    let field;
    if (this.orderGroup.value.customerSearchCriteria === 'customer-id') {
      field = 'externalId';
    } else {
      field = 'name.contains';
    }
    return field ? { [field]: val, addressType: type } : {};
  }

  private getLocationSearchCriteria(val: String) {
    const field = this.orderGroup.value.locationSearchCriteria === 'location-id' ? 'id' : 'name.contains';
    return { [field]: val };
  }

  private validateCustomerOrError(value: CustomerDto, control: AbstractControl): boolean {
    if (!value) {
      control.setErrors({ invalidCustomer: true });
      return false;
    }
    return true;
  }

  private loadCustomers(order: OrderDto) {
    if (order?.shippingCustomerId && order?.billingCustomerId) {
      this.customerService
        .getCustomerByCriteria({ 'id.in': [order.shippingCustomerId, order.billingCustomerId].join(',') })
        .subscribe(
          response => {
            const customers = response.body ?? [];

            this.selectShipCustomerTriggered = true;
            this.selectBillCustomerTriggered = true;

            this.selectedShipCustomer = customers.find(c => c.id === order.shippingCustomerId);
            this.selectedBillCustomer = customers.find(c => c.id === order.billingCustomerId);

            this.toOrderGroup(order);
          },
          err => {
            this.toaster.error('something-went-wrong.label');
            throw err;
          }
        );
    }

    if (order?.shippingLocationId) {
      this.locationService.getLocation(order.shippingLocationId).subscribe(response => {
        this.toOrderGroup(order);
        this.orderGroup.controls.shipToLocation.setValue(response.body);
      });
    }
  }

  private setProductCount() {
    this.productCountControl.setValue(this.products.length);
  }

  private loadNextOrderNumber(orderNumber: string) {
    this.orderGroup.patchValue({ orderNumber });
  }

  private loadLookupFields(lookUpRes: LookUpDto[]) {
    this.shippingMethods = lookUpRes.filter(lookUp => lookUp.type === ORDER_SHIPPING_METHOD);
    this.shipmentTypes = lookUpRes.filter(lookUp => lookUp.type === ORDER_SHIPMENT_TYPE);
    this.priorities = lookUpRes.filter(lookUp => lookUp.type === ORDER_DELIVERY_TYPE);
    this.labelingProdCategories = lookUpRes.filter(lookUp => lookUp.type === ORDER_PRODUCT_CATEGORY);
    this.serviceFees = lookUpRes.filter(lookUp => lookUp.type === ORDER_SERVICE_FEE);
    this.statuses = lookUpRes.filter(lookUp => lookUp.type === ORDER_STATUS);
    this.labelStatuses = lookUpRes.filter(lookUp => lookUp.type === ORDER_LABEL_STATUS);
  }

  private loadIconConfig(processProdRes: ProcessProductDto) {
    this.processProductConfig = processProdRes;
  }

  private loadProdAttributes(prodAttrRes: OrderProductAttributeDto[]) {
    this.productAttributes = prodAttrRes;
  }

  private setProductSelectionInfo() {
    const orderControls = this.orderGroup.controls;
    this.prodSelectionDescriptions = [
      { label: 'order-number.label', value: orderControls.orderNumber.value },
      { label: 'external-order-id.label', value: orderControls.externalId.value || 'N/A' },
      {
        label: this.internalTransferSelected ? 'location-id.label' : 'ship-to-customer-id.label',
        value: this.internalTransferSelected
          ? orderControls?.shipToLocation?.value.id
          : this.selectedShipCustomer?.externalId,
      },
      {
        label: this.internalTransferSelected ? 'ship-to-location.label' : 'ship-to-customer-name.label',
        value: this.internalTransferSelected
          ? orderControls?.shipToLocation?.value.name
          : this.selectedShipCustomer?.name,
      },
      {
        label: 'ship-date.label',
        value: orderControls.desireShippingDate.value
          ? moment(orderControls.desireShippingDate.value).format('MM/DD/YYYY')
          : '',
      },
    ];

    if (this.internalTransferSelected) {
      this.prodSelectionDescriptions.push({
        label: 'label-status.label',
        value: orderControls.labelStatus?.value,
      });
    }
  }

  private openAddEditProduct(product?: OrderProduct) {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '72rem';
    dialogConfig.data = {
      familyCategory: this.orderGroup.value.productCategoryKey,
      currentProduct: product,
      productAttributes: this.productAttributes,
    };
    const dialogRef = this.matDialog.open(AddProductModalComponent, dialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        (result.bloodTypeAndQuantity as BloodTypeAndQuantity[]).forEach(btAndQty => {
          const { bloodTypeAndQuantity, ...restResult } = result;
          this.addOrEditProduct(
            {
              ...restResult,
              ...btAndQty,
              rowIndex: product?.rowIndex,
            },
            product ? true : false
          );
        });
      }
    });
    dialogRef.beforeClosed().subscribe(result => {
      if (!result && this.route.snapshot.params.id) dialogRef.componentInstance.showCanceledChangesToasterMessage();
    });
  }

  private fillReviewAndSubmit() {
    this.orderInfoDescriptions = this.getOrderInfoDescriptions();
    this.billInfoDescriptions = !this.internalTransferSelected
      ? this.orderService.getBillInfoDescriptions(this.selectedBillCustomer)
      : [];
    this.shippingInfoDescriptions = this.orderService.getShippingInfoDescriptions(
      this.selectedShipCustomer,
      this.orderGroup.controls.shippingMethod.value,
      this.orderGroup.controls.desireShippingDate.value,
      this.shippingMethods,
      this.internalTransferSelected ? this.orderGroup.controls.shipToLocation.value : null
    );
    if (!this.internalTransferSelected) {
      this.feesInfoDescriptions = this.getOrderFeesInfoDescriptions();
    }
  }

  private getOrderInfoDescriptions(): Description[] {
    const form = this.orderGroup.value;
    const descriptions = [
      { label: 'order-number.label', value: this.orderGroup.controls.orderNumber.value },
      { label: 'priority.label', value: this.getLookUpDescriptionKey(form.deliveryType, this.priorities) },
      { label: 'external-order-id.label', value: this.orderGroup.controls.externalId.value || 'N/A' },
    ];
    if (this.isInternalTransfer(this.orderGroup.controls.shipmentType?.value)) {
      descriptions.push({
        label: 'label-status.label',
        value: form.labelStatus,
      });
    }
    return descriptions;
  }

  private getOrderFeesInfoDescriptions(): Description[] {
    return this.orderGroup.value.orderServiceFees.map(fee => {
      const feeDto = this.serviceFees.find(fDto => fee.serviceFee === fDto.optionValue);
      return { label: feeDto.descriptionKey, value: fee.quantity };
    });
  }

  private toOrderDto(): OrderDto {
    const formValues = this.orderGroup.value;
    const order = {
      ...formValues,
      desireShippingDate: moment(formValues.desireShippingDate).format('YYYY-MM-DDTHH:mm:ss'),
      priority: this.getDtoPriority(formValues.deliveryType),
      productCategoryKey: formValues.productCategoryKey,
      orderNumber: this.orderGroup.controls.orderNumber.value,
      shippingCustomerId: this.internalTransferSelected ? null : this.selectedShipCustomer.id,
      billingCustomerId: this.internalTransferSelected ? null : this.selectedBillCustomer.id,
      locationId: this.facilityService.getFacilityId(),
      orderItems: this.products.map(product => this.toOrderItemDto(product)),
      externalOrder: false,
      statusKey: OPEN_OPTION_VALUE,
      downtimeOrder: false,
      shipmentType: this.orderGroup.controls.shipmentType.value,
      shippingLocationId: this.internalTransferSelected ? this.orderGroup.controls.shipToLocation.value.id : null,
      labelStatus: this.internalTransferSelected ? formValues.labelStatus : ORDER_LABELED_OPTION,
    };

    if (!this.currentOrder) {
      order.externalId =
        this.orderGroup.controls.externalId.value === '' ? null : this.orderGroup.controls.externalId.value;
    } else {
      order.externalId = this.currentOrder.externalId;
    }

    return order;
  }

  private toOrderGroup(order: OrderDto) {
    this.currentOrder = {
      ...order,
      shipToCustomer: this.selectedShipCustomer?.name,
      billToCustomer: this.selectedBillCustomer?.name,
    };
    this.products = this.currentOrder?.orderItems?.map((item, index) => this.toOrderProduct(item, index)) ?? [];
    this.lockedLabelingProductCategoryValue = this.currentOrder.productCategoryKey;
    this.lockedLabelStatusValue = this.currentOrder.labelStatus;

    this.orderGroup.patchValue(this.currentOrder);
    this.orderGroup.patchValue({ shipToCustomer: this.selectedShipCustomer });
    this.orderGroup.patchValue({ billToCustomer: this.selectedBillCustomer });
    this.orderGroup.controls.externalId?.setValue(this.orderGroup.controls.externalId.value?.toUpperCase());
    this.orderGroup.controls.externalId?.disable();
    this.orderGroup.controls.customerSearchCriteria?.disable();
    this.orderGroup.controls.shipToCustomer?.disable();
    this.orderGroup.controls.billToCustomer?.disable();
    this.orderGroup.controls.locationSearchCriteria?.disable();
    this.orderGroup.controls.shipToLocation?.disable();
    this.setProductCount();
  }

  private toOrderItemDto(product: OrderProduct): OrderItemDto {
    return {
      quantity: product.quantity,
      productFamily: product.productFamily.familyValue,
      bloodTypeId: product.bloodType.id,
      filled: false,
      orderItemProductAttributes: this.getOrderItemProductAttributeDtoArray(product),
      comments: product.productComment,
    };
  }

  private toOrderProduct(item: OrderItemDto, index: number) {
    return <OrderProduct>{
      id: item.id,
      quantity: item.quantity,
      productComment: item.comments,
      productFamily: { familyValue: item.productFamily, descriptionKey: item.productFamily } as OrderProductFamilyDto,
      bloodType: this.bloodTypes.find(bt => bt.id === item.bloodTypeId),
      productAttributes: this.sortService.sortByDescriptionKey(
        this.productAttributes.filter(
          productAttribute =>
            item.orderItemProductAttributes.findIndex(
              orderProductAttribute => orderProductAttribute.productAttributeId === productAttribute.id
            ) > -1
        )
      ),
      antigensTested: this.antigenTested(item),
      rowIndex: index,
    };
  }

  private getDtoPriority(priority: string): number {
    return this.priorities.find(p => priority === p.optionValue).orderNumber || 99;
  }

  private getOrderItemProductAttributeDtoArray(product: OrderProduct): OrderItemProductAttributeDto[] {
    return product.productAttributes.map(pa => {
      let attributeOptions = [];
      if (pa.attributeValue === ANTIGEN_TESTED) {
        attributeOptions = product.antigensTested.map(antigen => {
          return {
            attributeOptionValue: antigen.optionValue,
          };
        });
      }
      return {
        productAttributeId: pa.id,
        productAttributeOptions: attributeOptions,
      };
    });
  }

  private antigenTested(item: OrderItemDto) {
    const antigenAttribute = this.productAttributes?.find(attr => attr.attributeValue === ANTIGEN_TESTED);

    const itemAntigenProdAttr = item.orderItemProductAttributes?.find(
      attr => attr.productAttributeId === antigenAttribute.id
    );

    return antigenAttribute.attributeOptions.filter(
      attr =>
        itemAntigenProdAttr?.productAttributeOptions?.findIndex(opt => opt.attributeOptionValue === attr.optionValue) >
        -1
    );
  }

  private getLookUpDescriptionKey(optionValue: string, list: LookUpDto[]) {
    return list.find(l => l.optionValue === optionValue)?.descriptionKey ?? '';
  }

  private isInternalTransfer(shipmentType: ShipmentType): boolean {
    return shipmentType === ShipmentType.INTERNAL;
  }

  changeShipmentType($event: ShipmentType) {
    this.addRemoveControlShipmentType($event);
  }

  get internalTransferSelected() {
    return this.isInternalTransfer(this.orderGroup.get('shipmentType').value);
  }

  private addRemoveControlShipmentType($event: ShipmentType) {
    if (this.isInternalTransfer($event)) {
      this.orderGroup.removeControl('customerSearchCriteria');
      this.orderGroup.removeControl('orderServiceFees');
      this.orderGroup.removeControl('shipToCustomer');
      this.orderGroup.removeControl('billToCustomer');

      this.orderGroup.addControl('shipToLocation', new FormControl(''));
      this.orderGroup.addControl('locationSearchCriteria', new FormControl(''));
      this.orderGroup.addControl(
        'labelStatus',
        new FormControl(
          this.labelStatuses.find(l => l.optionValue === ORDER_LABELED_OPTION).optionValue,
          Validators.required
        )
      );

      this.filteredLocations$ = this.orderGroup.get('shipToLocation').valueChanges.pipe(
        debounceTime(400),
        distinctUntilChanged(),
        map(val => (typeof val === 'string' ? val : null)), // Added because of `displayFn`, which makes the `valueChanges` to be called again.
        switchMap((searchString: string | null) => {
          if (searchString) {
            this.orderGroup.get('shipToLocation').disable();
            const locationFilter = this.getLocationSearchCriteria(searchString);
            return this.locationService
              .getAllLocationsByCriteria({ active: true, ...locationFilter, 'locationTypeId.in': 5 })
              .pipe(
                catchError(() => of({ body: [] } as HttpResponse<LocationDto[]>)),
                map(locationRes => {
                  this.orderGroup.get('shipToLocation').enable();
                  this.renderer.selectRootElement('#shipToLocation').focus();
                  const locations = locationRes.body ?? [];
                  if (!locations.length) {
                    this.orderGroup.controls.shipToLocation.setErrors({ noMatchingLocations: true });
                  } else {
                    this.orderGroup.controls.shipToLocation.setErrors(null);
                  }

                  return locations;
                })
              );
          }
          return of([]);
        }),
        takeUntil(this.orderGroup.get('shipmentType').valueChanges)
      );
    } else {
      this.orderGroup.addControl('customerSearchCriteria', new FormControl(''));
      this.orderGroup.addControl('orderServiceFees', new FormControl([]));
      this.orderGroup.addControl('shipToCustomer', new FormControl('', Validators.required));
      this.orderGroup.addControl('billToCustomer', new FormControl('', Validators.required));

      this.orderGroup.removeControl('shipToLocation');
      this.orderGroup.removeControl('locationSearchCriteria');
      this.orderGroup.removeControl('labelStatus');

      this.shipCustomers = this.getCustomerFilter(this.orderGroup.controls.shipToCustomer, ORDER_SHIPPING_CUSTOMER);
      this.billCustomers = this.getCustomerFilter(this.orderGroup.controls.billToCustomer, ORDER_BILLING_CUSTOMER);
    }
  }

  changeLabelStatus() {
    if (this.internalTransferSelected) {
      this.showCleanOrderProductsDialog(true);
    }
  }

  openUnlabeledConfirmDialogOutState() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '60rem';
    const dialogRef = this.matDialog.open<ConfirmDialogComponent>(ConfirmDialogComponent, dialogConfig);
    dialogRef.componentInstance.confirmMessage = 'unlabeled-products-out-state.message';
    dialogRef.afterClosed().subscribe(value => {
      if (value && this.orderGroup.valid) {
        this.stepper.next();
      }
    });
  }

  ngOnDestroy(): void {
    this.unsubscribeAll.next();
    this.unsubscribeAll.complete();
  }
}
