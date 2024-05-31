import { HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
  Description,
  ProcessHeaderService,
  ProcessProductDto,
  ShipmentInfoDto,
  ShipmentInfoItemDto,
  ShipmentService,
  ValidationType,
} from '@rsa/commons';
import { SortService } from '@rsa/distribution/core/services/sort.service';
import { SortEvent } from 'primeng/api';

@Component({
  selector: 'rsa-shipment-details',
  templateUrl: './shipment-details.component.html',
  styleUrls: ['./shipment-details.component.scss'],
})
export class ShipmentDetailsComponent implements OnInit {
  constructor(
    private _router: Router,
    private route: ActivatedRoute,
    public header: ProcessHeaderService,
    private shipmentService: ShipmentService,
    private sortService: SortService
  ) {}

  orderInfoDescriptions: Description[] = [];
  shippingInfoDescriptions: Description[] = [];
  shipmentInfo: ShipmentInfoDto;
  products: ShipmentInfoItemDto[] = [];
  processProductConfig: ProcessProductDto;
  readonly validationType = ValidationType;
  hasContentOrNot = true;

  get filledProductsCount() {
    return 0; // todo : should return number of filled products
  }

  get shipmentsCount() {
    return 0; // todo: should return length of shipment
  }

  get labelingProductCategory() {
    return this.shipmentInfo ? this.shipmentInfo?.productCategory : '';
  }

  get shipmentId() {
    return this.route.snapshot.params?.id;
  }

  get totalProducts(): number {
    return this.products.reduce<number>(
      (previousValue: number, currentValue: ShipmentInfoItemDto) => previousValue + +currentValue?.quantity,
      0
    );
  }

  ngOnInit(): void {
    const shipmentConfigData = this.route.snapshot.data?.shipmentDetailsConfigData as HttpResponse<ProcessProductDto>;
    if (shipmentConfigData?.body) {
      this.processProductConfig = shipmentConfigData.body as ProcessProductDto;
    }
    this.fetchShipmentDetails();
  }

  fetchShipmentDetails(): void {
    this.shipmentService.getShipmentById(this.shipmentId).subscribe(result => {
      this.shipmentInfo = result.body;
      this.products = this.shipmentInfo?.items?.map(item => this.convertItemToProduct(item)) ?? [];
      this.updateWidgets();
    });
  }

  private updateWidgets() {
    this.orderInfoDescriptions = this.shipmentService.getOrderInfoDescriptions(this.shipmentInfo);
    this.shippingInfoDescriptions = this.shipmentService.getShippingInfoDescriptions(this.shipmentInfo);
  }

  private convertItemToProduct(item: ShipmentInfoItemDto) {
    return <ShipmentInfoItemDto>{
      id: item.id,
      quantity: item.quantity,
      productComment: item.comments,
      productFamily: item.productFamily,
      bloodGroup: item.bloodType,
    };
  }

  backToSearch(): void {
    this._router.navigateByUrl('/orders/search');
  }

  fillOrder() {
    // TODO
  }

  viewPickList(): void {
    //TODO
  }

  customSort(event: SortEvent) {
    this.sortService.customSort(event);
  }

  getIcon(productFamily: string): string {
    return productFamily && this.processProductConfig?.properties[`icon.${productFamily}`]
      ? 'rsa:' + this.processProductConfig.properties[`icon.${productFamily}`]
      : 'rsa:product-whole-blood';
  }
}
