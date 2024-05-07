import { DOCUMENT } from '@angular/common';
import { HttpResponse } from '@angular/common/http';
import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { MatDialog, MatDialogConfig } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import {
  CustomerDto,
  CustomerService,
  Description,
  EmployeeService,
  FILL_ORDER_SHORT_DATE_PULL_LIST,
  INTERNAL_SHIPMENT_TYPE,
  INTERNAL_TRANSFER_ORDER_PACKING_SLIP,
  INTERNAL_TRANSFER_ORDER_REVIEW_REPORT_PRINT_TYPE,
  InventoryService,
  LocationDto,
  LocationService,
  LookUpDto,
  NotificationDto,
  OrderBloodTypeDto,
  OrderDto,
  OrderItemDto,
  OrderItemInventoryDto,
  OrderProductAttributeDto,
  OrderProductFamilyDto,
  OrderService,
  ORDER_PACKING_SLIP,
  ORDER_PRINT_TYPE,
  ORDER_SHIPPING_LABEL,
  PDF_EXTENSION,
  PrintService,
  PrintType,
  ProcessHeaderService,
  ProcessProductDto,
  ReasonDto,
  ROLE_CREATE_ORDER_ALL,
  ROLE_FILL_ORDER_ALL,
  ROLE_ORDERS_ALL,
  ShipmentDto,
  ShipmentService,
  TranslateInterpolationPipe,
  ValidateRuleDto,
  ValidationType,
} from '@rsa/commons';
import {
  ANTIGEN_TESTED,
  CANCELLED_OPTION_VALUE,
  CANCEL_REASON_ID,
  CLOSE_REASON_ID,
  OrderProduct,
  ORDER_DELIVERY_TYPE,
  ORDER_PRODUCT_CATEGORY,
  ORDER_SERVICE_FEE,
  ORDER_SHIPPING_METHOD,
  ORDER_STATUS,
  ShipmentType,
  SHIPPED_OPTION_VALUE,
} from '@rsa/distribution/core/models/orders.model';
import { SortService } from '@rsa/distribution/core/services/sort.service';
import { CancelOrderModalComponent } from '@rsa/distribution/modules/orders/cancel-order-modal/cancel-order-modal.component';
import { getAuthState } from '@rsa/global-data';
import { startCase } from 'lodash';
import { ToastrService } from 'ngx-toastr';
import { SortEvent } from 'primeng/api';
import { forkJoin, of, Subject } from 'rxjs';
import { catchError, combineAll, concatAll, map, switchMap, take, takeUntil, tap } from 'rxjs/operators';
import { CloseOrderModalComponent } from '../close-order-modal/close-order-modal.component';

@Component({
  selector: 'rsa-order-details',
  templateUrl: './order-details.component.html',
  styleUrls: ['./order-details.component.scss'],
})
export class OrderDetailsComponent implements OnInit, OnDestroy {
  constructor(
    private _router: Router,
    private route: ActivatedRoute,
    public header: ProcessHeaderService,
    protected fb: FormBuilder,
    private matDialog: MatDialog,
    private toaster: ToastrService,
    private orderService: OrderService,
    private customerService: CustomerService,
    private sortService: SortService,
    private printService: PrintService,
    private inventoryService: InventoryService,
    private store: Store,
    private translateInterpolationPipe: TranslateInterpolationPipe,
    @Inject(DOCUMENT) private document: Document,
    private shipmentService: ShipmentService,
    private employeeService: EmployeeService,
    private locationService: LocationService
  ) {
    store
      .select(getAuthState)
      .pipe(take(1))
      .subscribe(auth => {
        this.loggedUserId = auth['id'];
      });
  }

  get labelingProductCategory() {
    return (
      this.labelingProdCategories?.find(prodCat => prodCat.optionValue === this.currentOrder?.productCategoryKey)
        ?.descriptionKey ?? ''
    );
  }

  get isCancelOrderVisible() {
    return (
      this.currentOrder &&
      !this.isOrderCancelled &&
      !this.isOrderShipped &&
      !this.orderItemPendingToShip.length &&
      !this.shipmentsCount
    );
  }

  get isEditOrderVisible() {
    return this.currentOrder && !this.isOrderCancelled && !this.isOrderShipped && !this.filledProductsCount;
  }

  get isFillOrderVisible() {
    return this.currentOrder && !this.isOrderCancelled && !this.isOrderShipped;
  }

  get isValidateVisible() {
    return this.orderItemPendingToShip.length && !this.isOrderCancelled && !this.isOrderShipped;
  }

  get isPrintPullListVisible() {
    return this.currentOrder && !this.isOrderCancelled && !this.isOrderShipped;
  }

  get isCloseOrderVisible() {
    return this.currentOrder && this.shipmentsCount && !this.isOrderCancelled && !this.isOrderShipped;
  }

  get isOrderShipped() {
    return this.currentOrder.statusKey === SHIPPED_OPTION_VALUE;
  }

  get isOrderCancelled() {
    return this.currentOrder.statusKey === CANCELLED_OPTION_VALUE;
  }

  get filledProductsCount() {
    return this.orderItemInventories?.length || 0;
  }

  get shipmentsCount() {
    return this.shipments?.length || 0;
  }

  get totalProducts(): number {
    return this.products.reduce<number>(
      (previousValue: number, currentValue: OrderProduct) => previousValue + +currentValue?.quantity,
      0
    );
  }

  get totalShippedProducts(): number {
    return this.shipments.reduce<number>(
      (previousValue: number, currentValue: ShipmentDto) => previousValue + +currentValue?.shipmentItems?.length,
      0
    );
  }

  get orderId() {
    return this.route.snapshot.params?.id;
  }
  readonly validationType = ValidationType;

  loading = false;
  orderInfoDescriptions: Description[] = [];
  billInfoDescriptions: Description[] = [];
  shippingInfoDescriptions: Description[] = [];
  feesInfoDescriptions: Description[] = [];
  currentOrder: OrderDto;
  statuses: LookUpDto[] = [];
  deliveryTypes: LookUpDto[] = [];
  shippingMethods: LookUpDto[] = [];
  labelingProdCategories: LookUpDto[] = [];
  serviceFees: LookUpDto[] = [];
  bloodTypes: OrderBloodTypeDto[] = [];
  productAttributes: OrderProductAttributeDto[] = [];
  cancellationReasons: ReasonDto[] = [];
  closingReasons: ReasonDto[] = [];
  products: OrderProduct[] = [];
  orderItemInventories: OrderItemInventoryDto[] = [];
  orderItemPendingToShip: OrderItemInventoryDto[] = [];
  processProductConfig: ProcessProductDto;
  shipments: ShipmentDto[] = [];
  loggedUserId: string;
  shippingLocation: LocationDto;

  destroy$ = new Subject<any>();

  protected readonly ROLE_CREATE_ORDER_ALL = ROLE_CREATE_ORDER_ALL;
  protected readonly ROLE_ORDERS_ALL = ROLE_ORDERS_ALL;
  protected readonly ROLE_FILL_ORDER_ALL = ROLE_FILL_ORDER_ALL;

  ngOnInit(): void {
    const orderConfigData = this.route.snapshot.data?.orderDetailsConfigData as HttpResponse<
      LookUpDto[] | OrderBloodTypeDto[] | OrderProductAttributeDto[] | ReasonDto[] | ProcessProductDto
    >[];

    if (orderConfigData?.length) {
      const lookUpRes = orderConfigData[0];
      const bloodTypeRes = orderConfigData[1];
      const productAttributesRes = orderConfigData[2];
      const reasonRes = orderConfigData[3];

      this.statuses = (lookUpRes?.body as LookUpDto[])?.filter(lookUp => lookUp.type === ORDER_STATUS);
      this.deliveryTypes = (lookUpRes?.body as LookUpDto[])?.filter(lookUp => lookUp.type === ORDER_DELIVERY_TYPE);
      this.shippingMethods = (lookUpRes?.body as LookUpDto[])?.filter(lookUp => lookUp.type === ORDER_SHIPPING_METHOD);
      this.labelingProdCategories = (lookUpRes?.body as LookUpDto[])?.filter(
        lookUp => lookUp.type === ORDER_PRODUCT_CATEGORY
      );
      this.serviceFees = (lookUpRes?.body as LookUpDto[])?.filter(lookUp => lookUp.type === ORDER_SERVICE_FEE);
      this.bloodTypes = bloodTypeRes?.body as OrderBloodTypeDto[];
      this.productAttributes = productAttributesRes?.body as OrderProductAttributeDto[];
      this.cancellationReasons = (reasonRes?.body as ReasonDto[]).filter(
        reason => reason.reasonType === CANCEL_REASON_ID
      );
      this.closingReasons = (reasonRes?.body as ReasonDto[]).filter(reason => reason.reasonType === CLOSE_REASON_ID);
      this.processProductConfig = orderConfigData[4]?.body as ProcessProductDto;
    }

    this.fetchOrder();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  fetchOrder() {
    this.orderService
      .getOrderById(this.orderId)
      .pipe(
        takeUntil(this.destroy$),
        switchMap(orderRes => {
          this.currentOrder = orderRes.body;
          const customerIds = [`${this.currentOrder.shippingCustomerId}`, `${this.currentOrder.billingCustomerId}`];
          const shippingLocationId = this.currentOrder.shippingLocationId;

          this.fetchShipmentAndAggregateEmployee();

          return forkJoin([
            !this.internalTransfer()
              ? this.customerService
                  .getCustomerByCriteria({ 'id.in': customerIds.join(',') })
                  .pipe(catchError(() => of({ body: [] } as HttpResponse<CustomerDto[]>)))
              : of({ body: [] }),
            this.orderService
              .getOrderItemInventoryByCriteria({ order: this.currentOrder.id, filled: true, size: 1000 })
              .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderItemInventoryDto[]>))),
            this.orderService
              .getOrderItemInventoriesPendingToShipByCriteria(
                {
                  orderId: this.currentOrder.id,
                  filled: true,
                  withEmployeeId: true,
                },
                this.currentOrder.labelStatus === 'UNLABELED'
              )
              .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderItemInventoryDto[]>))),
            shippingLocationId
              ? this.locationService
                  .getLocation(shippingLocationId)
                  .pipe(catchError(() => of({ body: null } as HttpResponse<LocationDto>)))
              : of(null),
          ]);
        })
      )
      .subscribe(
        ([customerRes, orderItemInventoryRes, orderItemPendingToShip, locationRes]) => {
          this.shippingLocation = locationRes?.body;
          const customers = customerRes.body;
          this.updateWidgets(
            customers?.length ? customers.find(customer => customer.id === this.currentOrder.shippingCustomerId) : null,
            customers?.length ? customers.find(customer => customer.id === this.currentOrder.billingCustomerId) : null
          );
          this.orderItemInventories = orderItemInventoryRes.body;
          this.products = this.currentOrder?.orderItems?.map(item => this.convertOrderItemToOrderProduct(item)) ?? [];
          this.orderItemPendingToShip = orderItemPendingToShip.body;
        },
        err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        }
      );
  }

  getOrderReviewPrintType(): PrintType {
    switch (this.currentOrder?.shipmentType) {
      case ShipmentType.INTERNAL:
        return INTERNAL_TRANSFER_ORDER_REVIEW_REPORT_PRINT_TYPE;
      default:
        return ORDER_PRINT_TYPE;
    }
  }

  getOrderPackingSlipPrintType(): PrintType {
    switch (this.currentOrder?.shipmentType) {
      case ShipmentType.INTERNAL:
        return INTERNAL_TRANSFER_ORDER_PACKING_SLIP;
      default:
        return ORDER_PACKING_SLIP;
    }
  }

  printOrder() {
    const printType = this.getOrderReviewPrintType();
    const params = { referenceId: this.currentOrder.id };
    this.printService.print(PDF_EXTENSION, printType, params).subscribe(response => {
      if (response?.body?.file) {
        const a = this.document.createElement('a');
        a.href = `data:application/pdf;base64,${response.body.file}`;
        a.download = `Order-${this.currentOrder.orderNumber}`;
        a.click();
      }
    });
  }

  editOrder() {
    this._router.navigateByUrl(`/orders/${this.currentOrder?.id}/edit`);
  }

  pullList() {
    this.printPdf(FILL_ORDER_SHORT_DATE_PULL_LIST, `PullList-${this.currentOrder.id}`, {
      orderId: this.currentOrder.id,
      shipmentType: this.currentOrder.shipmentType,
    });
  }

  cancelOrder() {
    const dialogConfig = new MatDialogConfig();
    dialogConfig.width = '24rem';
    const dialogRef = this.matDialog.open(CancelOrderModalComponent, dialogConfig);
    dialogRef.componentInstance.cancellationReasons = this.cancellationReasons;
    dialogRef.afterClosed().subscribe(reasonId => {
      if (reasonId) {
        this.orderService.cancelOrder(this.currentOrder.id, reasonId).subscribe(
          () => {
            this.back();
            this.toaster.success('the-order-has-been-cancelled.label');
          },
          err => {
            this.toaster.error('something-went-wrong.label');
            throw err;
          }
        );
      }
    });
  }

  fillOrder(product: OrderProduct) {
    this.inventoryService.validate(this.getValidateRuleDto(product)).subscribe(
      response => {
        const value = response.body;
        const notifications = value.notifications;
        const url = value._links?.next;

        if (notifications?.length) {
          if (notifications[0].notificationType === 'success') {
            if (url) {
              this._router.navigateByUrl(url, {
                replaceUrl: true,
                state: {
                  product,
                  currentOrder: this.currentOrder,
                  totalShippedProducts: this.getTotalShippedProductsByOrderItem(product.id),
                  prodIcon: this.getIcon(product?.productFamily?.descriptionKey),
                  orderInfoDescriptions: this.orderInfoDescriptions,
                  billInfoDescriptions: this.billInfoDescriptions,
                  shippingInfoDescriptions: this.shippingInfoDescriptions,
                  feesInfoDescriptions: this.feesInfoDescriptions,
                },
              });
            }
          } else {
            this.displayMessageFromNotificationDto(notifications[0]);
          }
        }
      },
      err => {
        this.toaster.error('something-went-wrong.label');
        throw err;
      }
    );
  }

  getTotalShippedProductsByOrderItem(orderItemId: number) {
    let orderItemShippedProductsCount = 0;

    const orderItemInventoriesIds =
      this.orderItemInventories?.filter(inv => inv.orderItem === orderItemId)?.map(inv => inv.inventoryId) || [];

    this.shipments?.forEach(shipment => {
      const shipmentInventoriesIds = shipment.shipmentItems?.map(item => item.inventoryId) || [];

      orderItemShippedProductsCount += orderItemInventoriesIds.reduce(
        (previousValue: number, id: number) =>
          shipmentInventoriesIds.includes(id) ? previousValue + 1 : previousValue,
        0
      );
    });

    return orderItemShippedProductsCount;
  }

  validateOrder() {
    this._router.navigateByUrl(`/orders/${this.currentOrder?.id}/validate`);
  }

  printSlip(shipment: any) {
    const printType = this.getOrderPackingSlipPrintType();
    const params = this.loadParams(shipment);
    this.printPdf(printType, `PackingSlipLabel-${shipment.id}`, params);
  }

  printLabel(shipment: any) {
    const params = this.loadParams(shipment);
    this.printPdf(ORDER_SHIPPING_LABEL, `ShippingLabel-${shipment.id}`, params);
  }

  closeOrder() {
    this.inventoryService.validate(this.getCloseOrderValidateRuleDto()).subscribe(response => {
      const value = response.body;
      const notifications = value.notifications;
      const results = value.results;

      if (notifications?.length) {
        if (notifications[0].notificationType === 'success' && results.requiredReason[0] === false) {
          this.displaySuccessMessageAndReloadOrder();
        } else if (notifications[0].notificationType === 'success') {
          const dialogConfig = new MatDialogConfig();
          dialogConfig.width = '24rem';
          const dialogRef = this.matDialog.open(CloseOrderModalComponent, dialogConfig);
          dialogRef.componentInstance.closeOrderReasons = this.closingReasons;
          dialogRef.afterClosed().subscribe(reasonId => {
            if (reasonId) {
              this.orderService.closeOrder(this.currentOrder.id, reasonId).subscribe(
                () => {
                  this.displaySuccessMessageAndReloadOrder();
                },
                err => {
                  this.toaster.error('something-went-wrong.label');
                  throw err;
                }
              );
            }
          });
        } else {
          this.displayMessageFromNotificationDto(notifications[0]);
        }
      }
    });
  }

  displayMessageFromNotificationDto(notification: NotificationDto) {
    this.toaster.show(
      this.translateInterpolationPipe.transform(notification.message, []),
      startCase(notification.notificationType),
      {},
      notification.notificationType
    );
  }

  displaySuccessMessageAndReloadOrder() {
    this.toaster.success('the-order-has-been-closed.label');
    this.fetchOrder();
  }

  back() {
    this._router.navigateByUrl('/orders/search');
  }

  customSort(event: SortEvent) {
    this.sortService.customSort(event);
  }

  getIcon(productFamily: string): string {
    return productFamily && this.processProductConfig?.properties[`icon.${productFamily}`]
      ? 'rsa:' + this.processProductConfig.properties[`icon.${productFamily}`]
      : 'rsa:product-whole-blood';
  }

  private updateWidgets(shippingCustomer: CustomerDto, billingCustomer: CustomerDto) {
    this.orderInfoDescriptions = this.orderService.getOrderInfoDescriptions(
      this.currentOrder,
      this.deliveryTypes,
      this.statuses,
      this.labelingProductCategory
    );

    this.billInfoDescriptions = billingCustomer ? this.orderService.getBillInfoDescriptions(billingCustomer) : [];

    this.shippingInfoDescriptions = this.orderService.getShippingInfoDescriptions(
      shippingCustomer,
      this.currentOrder?.shippingMethod,
      this.currentOrder?.desireShippingDate,
      this.shippingMethods,
      this.internalTransfer() ? this.shippingLocation : null
    );

    this.feesInfoDescriptions = this.orderService.getServiceFeesInfoDescriptions(
      this.currentOrder?.orderServiceFees,
      this.serviceFees
    );
  }

  private convertOrderItemToOrderProduct(item: OrderItemDto) {
    return <OrderProduct>{
      id: item.id,
      quantity: item.quantity,
      productComment: item.comments,
      productFamily: { descriptionKey: item.productFamily } as OrderProductFamilyDto,
      bloodType: this.bloodTypes.find(bt => bt.id === item.bloodTypeId),
      attachments: item.orderItemAttachments,
      productAttributes: this.sortService.sortByDescriptionKey(
        this.productAttributes.filter(
          productAttribute =>
            item.orderItemProductAttributes.findIndex(
              orderProductAttribute => orderProductAttribute.productAttributeId === productAttribute.id
            ) > -1
        )
      ),
      antigensTested: this.antigenTested(item),
      quantityFilledProducts: this.orderItemInventories?.filter(inv => inv.orderItem === item.id)?.length || 0,
    };
  }

  private antigenTested(item: OrderItemDto) {
    const antigenAttribute = this.productAttributes?.find(attr => attr.attributeValue === ANTIGEN_TESTED);

    const itemAntigenProdAttr = item.orderItemProductAttributes?.find(
      attr => attr.productAttributeId === antigenAttribute?.id
    );

    return antigenAttribute?.attributeOptions?.filter(
      attr =>
        itemAntigenProdAttr?.productAttributeOptions?.findIndex(opt => opt.attributeOptionValue === attr.optionValue) >
        -1
    );
  }

  private getCloseOrderValidateRuleDto() {
    return <ValidateRuleDto>{
      ruleName: 'rul-0092-close-order-validation',
      orderId: this.currentOrder.id,
    };
  }

  private getValidateRuleDto(product: OrderProduct) {
    return <ValidateRuleDto>{
      ruleName: 'fill-order',
      orderId: this.currentOrder.id,
      orderItemId: product.id,
      employeeId: this.loggedUserId,
    };
  }

  private fetchShipmentAndAggregateEmployee() {
    this.shipmentService
      .getShipmentByCriteria({ orderId: this.orderId })
      .pipe(
        map(response => response.body || []),
        tap((shipments: ShipmentDto[]) => {
          this.shipments = shipments;
        }),
        concatAll(),
        map((shipment: ShipmentDto) =>
          this.employeeService.getEmployeeById(shipment.employeeId).pipe(
            tap(r => (shipment.employee = r.body)),
            map(employee => ({
              ...shipment,
              employee: employee?.body,
            }))
          )
        ),
        combineAll(),
        tap((r: ShipmentDto[]) => {
          this.shipments = r;
        })
      )
      .subscribe();
  }

  private printPdf(printType: PrintType, documentName: string, params: { [key: string]: any }) {
    this.printService.print(PDF_EXTENSION, printType, params).subscribe(
      response => {
        if (response?.body?.file) {
          const a = this.document.createElement('a');
          a.href = `data:application/pdf;base64,${response.body.file}`;
          a.download = `${documentName}-${this.getDate()}.pdf`;
          a.click();
        }
      },
      err => this.toaster.error('something-went-wrong.label')
    );
  }

  private getDate() {
    return new Date().toISOString().split('T')[0];
  }

  private loadParams(shipment: ShipmentDto): Record<string, string | number> {
    const params: Record<string, string | number> = {
      shipmentId: shipment.id,
    };
    if (this.internalTransfer()) {
      params.shipmentType = INTERNAL_SHIPMENT_TYPE;
    }
    return params;
  }

  private internalTransfer(): boolean {
    return this.currentOrder.shipmentType.toUpperCase() === INTERNAL_SHIPMENT_TYPE;
  }
}
