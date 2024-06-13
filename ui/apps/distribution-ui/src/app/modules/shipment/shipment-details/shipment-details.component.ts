import { HttpResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ActivatedRoute, Router } from '@angular/router';
import {
  Description,
  PackingListService,
  ProcessHeaderService,
  ProcessProductDto,
  ShipmentInfoDto,
  ShipmentInfoItemDto,
  ShipmentService,
  ValidationType,
} from '@rsa/commons';
import { SortService } from '@rsa/distribution/core/services/sort.service';
import { ViewPickListComponent } from '@rsa/distribution/modules/shipment/view-pick-list/view-pick-list.component';
import { SortEvent } from 'primeng/api';
import { of } from 'rxjs';
import {
  DEFAULT_PAGE_SIZE,
  DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
  DEFAULT_PAGE_SIZE_DIALOG_WIDTH,
} from '@rsa/distribution/core/print-section/browser-printing.model';
import {
  ViewPackingListComponent
} from '@rsa/distribution/modules/shipment/view-packing-list/view-packing-list.component';
import { catchError, switchMap } from 'rxjs/operators';
import { BrowserPrintingService } from '@rsa/distribution/core/print-section/browser-printing.service';
import {
  ViewShippingLabelComponent
} from '@rsa/distribution/modules/shipment/view-shipping-label/view-shipping-label.component';
import { ToastrService } from 'ngx-toastr';

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
    private sortService: SortService,
    private packingListService: PackingListService,
    private toaster: ToastrService,
    private matDialog: MatDialog,
    private browserPrintService: BrowserPrintingService,
  ) {}

  orderInfoDescriptions: Description[] = [];
  shippingInfoDescriptions: Description[] = [];
  shipmentInfo: ShipmentInfoDto;
  products: ShipmentInfoItemDto[] = [];
  processProductConfig: ProcessProductDto;
  readonly validationType = ValidationType;

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
      comments: item.comments,
      productFamily: item.productFamily,
      bloodType: item.bloodType,
    };
  }

  backToSearch(): void {
    this._router.navigateByUrl('/orders/search');
  }

  fillProducts(item: ShipmentInfoItemDto): void {
    const url = `shipment/${this.shipmentId}/fill-products/${item.id}`;
    this._router.navigateByUrl(url);
  }

  viewPickList(): void {
    const dialogRef = this.matDialog.open(ViewPickListComponent, {
      id: 'ViewPickListDialog',
      width: DEFAULT_PAGE_SIZE_DIALOG_WIDTH,
      height: DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
    });
    dialogRef.componentInstance.model$ = of(this.shipmentInfo);
  }

  viewPackingList(print?: boolean): void {
    let dialogRef: MatDialogRef<ViewPackingListComponent>;
    this.packingListService
      .generatePackingListLabel(this.shipmentInfo.id)
      .pipe(
        switchMap(response => {
          const packingListLabel = response?.data?.generatePackingListLabel;
          dialogRef = this.matDialog.open(ViewPackingListComponent, {
            id: 'ViewPackingListDialog',
            ...(print
                ? {
                  hasBackdrop: false,
                  panelClass: 'hidden',
                }
                : {
                  width: DEFAULT_PAGE_SIZE_DIALOG_WIDTH,
                  height: DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
                }
            )
          });
          dialogRef.componentInstance.model$ = of(packingListLabel);
          return dialogRef.afterOpened();
        }),
        catchError(err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        }),
      )
      .subscribe(() => {
        this.browserPrintService.print(
          'viewPackingListReport',
          { pagesize: DEFAULT_PAGE_SIZE }
        );
        dialogRef?.close();
      });
  }

  viewShippingLabel(print?: boolean): void {
    let dialogRef: MatDialogRef<ViewShippingLabelComponent>;
    this.packingListService
      .generateShippingLabel(this.shipmentInfo.id)
      .pipe(
        switchMap(response => {
          const packingListLabel = response?.data?.generateShippingLabel;
          dialogRef = this.matDialog.open(ViewShippingLabelComponent, {
            id: 'ViewShippingLabelDialog',
            ...(print
                ? {
                  hasBackdrop: false,
                  panelClass: 'hidden',
                }
                : {
                  width: DEFAULT_PAGE_SIZE_DIALOG_WIDTH,
                  height: DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
                }
            )
          });
          dialogRef.componentInstance.model$ = of(packingListLabel);
          return dialogRef.afterOpened();
        }),
        catchError(err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        }),
      )
      .subscribe(() => {
        this.browserPrintService.print(
          'viewShippingLabelReport',
          { pagesize: DEFAULT_PAGE_SIZE }
        );
        dialogRef?.close();
      });
  }

  customSort(event: SortEvent) {
    this.sortService.customSort(event);
  }

  getIcon(productFamily: string): string {
    return productFamily && this.processProductConfig?.properties[`icon.${productFamily}`]
      ? 'rsa:' + this.processProductConfig.properties[`icon.${productFamily}`]
      : 'rsa:product-whole-blood';
  }

  loadLabel(shipmentId: number): void {
    this.packingListService.getLabel(shipmentId).subscribe(response => {
      const packingListLabel = response.data.generatePackingListLabel;
      console.log(packingListLabel);
      // FIXME example
    });
  }

  completeShipment() {
    //Todo
  }
}
