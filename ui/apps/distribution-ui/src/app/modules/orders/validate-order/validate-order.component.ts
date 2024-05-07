import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import {
  BarcodeService,
  CustomerDto,
  Description,
  INTERNAL_SHIPMENT_TYPE,
  InventoryService,
  LabelStatus,
  LocationAddressDto,
  LocationDto,
  LocationService,
  LookUpDto,
  OrderDto,
  OrderItemInventoryDto,
  OrderService,
  ProcessHeaderService,
  ShipmentDto,
  ShipmentItemDto,
  ShipmentService,
  ValidationType,
} from '@rsa/commons';
import {
  CANCELLED_OPTION_VALUE,
  ORDER_DELIVERY_TYPE,
  ORDER_PRODUCT_CATEGORY,
  ORDER_SERVICE_FEE,
  ORDER_SHIPPING_CUSTOMER,
  ORDER_SHIPPING_METHOD,
  ORDER_STATUS,
  ShipmentType,
  SHIPPED_OPTION_VALUE,
} from '@rsa/distribution/core/models/orders.model';
import { ProductSelectionComponent } from '@rsa/distribution/shared/components/product-selection/product-selection.component';
import { ToastrService } from 'ngx-toastr';
import { catchError, finalize } from 'rxjs/operators';

@Component({
  selector: 'rsa-validate-order',
  templateUrl: './validate-order.component.html',
  styleUrls: ['./validate-order.component.scss'],
})
export class ValidateOrderComponent implements OnInit {
  readonly validationType = ValidationType;

  @ViewChild('productSelectionComponent') productSelection: ProductSelectionComponent;

  products: OrderItemInventoryDto[] = [];
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
  unitNumberFocus = true;
  productCodeFocus = false;
  shippingCustomer: CustomerDto;
  billingCustomer: CustomerDto;
  shipments: ShipmentDto[];
  locationAddress: LocationAddressDto;
  shippingLocation: LocationDto;

  constructor(
    private _router: Router,
    private route: ActivatedRoute,
    public header: ProcessHeaderService,
    protected fb: FormBuilder,
    private orderService: OrderService,
    private barcodeService: BarcodeService,
    private shipmentService: ShipmentService,
    private toasterService: ToastrService,
    private inventoryService: InventoryService,
    private locationService: LocationService
  ) {}

  get getValidatedInventoryIds() {
    return this.products.filter(p => p.validated).map(p => p.inventoryId);
  }

  ngOnInit(): void {
    const validateData = this.route.snapshot.data.validateData;
    this.currentOrder = validateData.order;
    this.shippingCustomer = validateData.shippingCustomer;
    this.billingCustomer = validateData.billingCustomer;
    this.statuses = validateData.lookups.filter(lookUp => lookUp.type === ORDER_STATUS);
    this.deliveryTypes = validateData.lookups.filter(lookUp => lookUp.type === ORDER_DELIVERY_TYPE);
    this.shippingMethods = validateData.lookups.filter(lookUp => lookUp.type === ORDER_SHIPPING_METHOD);
    this.labelingProdCategories = validateData.lookups.filter(lookUp => lookUp.type === ORDER_PRODUCT_CATEGORY);
    this.serviceFees = validateData.lookups.filter(lookUp => lookUp.type === ORDER_SERVICE_FEE);
    this.shipments = validateData.shipments;
    this.products = validateData.products;
    if (this.currentOrder.shipmentType === ShipmentType.INTERNAL) this.getLocationAddress(this.currentOrder.locationId);

    if (this.currentOrder.shippingLocationId) {
      this.getShippingLocation(this.currentOrder.shippingLocationId).subscribe(location => {
        this.shippingLocation = location.body;
        this.updateWidgets();
      });
    } else {
      this.updateWidgets();
    }
  }

  back() {
    this._router.navigateByUrl(`/orders/${this.route.snapshot.params?.id}/details`);
  }

  updateLoadingState = (value: boolean) => (this.loading = value);

  unitNumberProductCodeSelected(unitNumber: string, productCode: string) {
    const labelStatus = this.isLabeledOrder ? 'LABELED' : 'UNLABELED';
    this.inventoryService
      .validate({
        unitNumber: unitNumber,
        isbtProductCode: labelStatus === 'LABELED' ? productCode : null,
        productCode: labelStatus === 'UNLABELED' ? productCode : null,
        labelStatus,
        ruleName: 'rul-0091-fillorder-validate-product',
        shipmentType: this.currentOrder?.shipmentType?.toUpperCase(),
        orderId: this.currentOrder.id,
      })
      .pipe(
        catchError(err => {
          this.showErrorToaster();
          throw err;
        }),
        finalize(() => {
          this.loading = false;
          this.unitNumberFocus = true;
          this.productSelection.resetFormGroup();
        })
      )
      .subscribe(response => {
        const responseWithNotifications = !!response?.body?.notifications?.length;
        if (!responseWithNotifications) {
          return;
        }

        const notification = response.body.notifications[0];
        const product = this.products.find(p => p.productCode === productCode && p.unitNumber === unitNumber);
        if (notification.statusCode === '200' && product) {
          product.validated = true;
          return;
        }

        this.products.forEach(p => delete p.validated);
        this.toasterService.show(
          notification.message,
          notification.notificationType,
          {},
          notification.notificationType
        );
      });
  }

  completeOrder() {
    if (this.currentOrder.shipmentType === ShipmentType.INTERNAL) {
      this.createShipment(this.buildShipmentDtoForInternalTransfer());
    } else {
      this.createShipment(this.buildShipmentDto());
    }
  }

  get totalProducts(): number {
    return this.products.length;
  }

  get validatedProducts(): number {
    return this.products.reduce<number>(
      (previousValue: number, currentValue: OrderItemInventoryDto) => previousValue + (currentValue?.validated ? 1 : 0),
      0
    );
  }

  get failedProducts(): boolean {
    return this.products.some(prod => !prod?.validated);
  }

  get isOrderShipped() {
    return this.currentOrder.statusKey === SHIPPED_OPTION_VALUE;
  }

  get isOrderCancelled() {
    return this.currentOrder.statusKey === CANCELLED_OPTION_VALUE;
  }

  get shippedProductsCount() {
    return this.shipments?.length || 0;
  }

  private createShipment(shipmentDto: ShipmentDto) {
    this.shipmentService.createShipment(shipmentDto).subscribe(
      response => {
        if (response.body) {
          this.toasterService.success('shipment-created-successfully.label');
          this._router.navigateByUrl(`/orders/${this.route.snapshot.params?.id}/details`);
        } else {
          this.showErrorToaster();
        }
      },
      err => {
        this.showErrorToaster();
      }
    );
  }

  private getLocationAddress(locationId: number) {
    const criteria = { active: true, locationId: locationId };
    return this.locationService.getAllLocationAddressByCriteria(criteria).subscribe(
      response => {
        this.locationAddress = response.body[0];
      },
      err => {
        this.showErrorToaster();
      }
    );
  }

  private getShippingLocation = (shippingLocationId: number) => this.locationService.getLocation(shippingLocationId);

  private internalTransfer(): boolean {
    return this.currentOrder?.shipmentType?.toUpperCase() === INTERNAL_SHIPMENT_TYPE;
  }

  get isLabeledOrder() {
    return this.currentOrder && this.currentOrder.labelStatus && this.currentOrder.labelStatus !== 'UNLABELED';
  }

  private updateWidgets() {
    this.billInfoDescriptions = this.billingCustomer
      ? this.orderService.getBillInfoDescriptions(this.billingCustomer)
      : [];
    this.orderInfoDescriptions = this.orderService.getOrderInfoDescriptions(
      this.currentOrder,
      this.deliveryTypes,
      this.statuses,
      this.labelingProdCategories.find(cat => cat.optionValue === this.currentOrder.productCategoryKey).descriptionKey
    );
    this.shippingInfoDescriptions = this.orderService.getShippingInfoDescriptions(
      this.shippingCustomer,
      this.currentOrder?.shippingMethod,
      this.currentOrder?.desireShippingDate,
      this.shippingMethods,
      this.internalTransfer() ? this.shippingLocation : null
    );
    this.feesInfoDescriptions = this.orderService.getServiceFeesInfoDescriptions(
      this.currentOrder.orderServiceFees,
      this.serviceFees
    );
  }

  private showErrorToaster(message: string = 'something-went-wrong.label') {
    this.toasterService.error(message);
  }

  private buildShipmentItemDtos(dtos: OrderItemInventoryDto[]): ShipmentItemDto[] {
    return dtos.map(dto => ({ inventoryId: dto.inventoryId }));
  }

  private buildShipmentDto(): ShipmentDto {
    return {
      ...this.shippingCustomer.customerAddresses.filter(address => address.addressType === ORDER_SHIPPING_CUSTOMER)[0],
      ...this.currentOrder,
      id: null,
      shipmentMethod: this.currentOrder.shippingMethod,
      orderId: this.currentOrder.id,
      shipmentItems: this.buildShipmentItemDtos(this.products),
    };
  }

  private buildShipmentDtoForInternalTransfer(): ShipmentDto {
    return {
      ...this.currentOrder,
      ...this.locationAddress,
      id: null,
      locationIdTo: this.currentOrder.shippingLocationId,
      customerId: null,
      shipmentMethod: this.currentOrder.shippingMethod,
      orderId: this.currentOrder.id,
      shipmentItems: this.buildShipmentItemDtos(this.products),
      city: this.locationAddress.city,
      countryCode: this.locationAddress.country,
      addressLine1: this.locationAddress.lines?.length > 0 ? this.locationAddress.lines[0] : '',
      addressLine2: this.locationAddress.lines?.length > 1 ? this.locationAddress.lines[1] : '',
    };
  }
}
