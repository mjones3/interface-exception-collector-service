import { AsyncPipe, CommonModule, formatDate } from '@angular/common';
import { Component, Inject, LOCALE_ID, OnInit } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { ProcessHeaderComponent } from 'app/shared/components/process-header/process-header.component';
import { Description } from 'app/shared/models/description.model';
import { NotificationDto } from 'app/shared/models/notification.dto';
import { ProcessProductDto } from 'app/shared/models/process-product.dto';
import { ValidationType } from 'app/shared/pipes/validation.pipe';
import { ProcessHeaderService } from 'app/shared/services/process-header.service';
import { SortService } from 'app/shared/services/sort.service';
import { startCase } from 'lodash-es';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { SortEvent } from 'primeng/api';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { of, switchMap } from 'rxjs';
import { catchError, take } from 'rxjs/operators';
import { ERROR_MESSAGE } from '../../../../../public/i18n/common-labels';
import {
  DEFAULT_PAGE_SIZE,
  DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
  DEFAULT_PAGE_SIZE_DIALOG_WIDTH,
} from '../../../core/services/browser-printing/browser-printing.model';
import { BrowserPrintingService } from '../../../core/services/browser-printing/browser-printing.service';
import {
  FilledProductInfoDto,
  ShipmentCompleteInfoDto,
  ShipmentInfoDto,
  ShipmentInfoItemDto,
} from '../models/shipment-info.dto';
import { PackingListService } from '../services/packing-list.service';
import { ShipmentService } from '../services/shipment.service';
import { ShippingLabelService } from '../services/shipping-label.service';
import { OrderWidgetsSidebarComponent } from '../shared/order-widgets-sidebar/order-widgets-sidebar.component';
import { ViewPackingListComponent } from '../view-packing-list/view-packing-list.component';
import { ViewPickListComponent } from '../view-pick-list/view-pick-list.component';
import { ViewShippingLabelComponent } from '../view-shipping-label/view-shipping-label.component';

@Component({
  selector: 'app-shipment-details',
  standalone: true,
  imports: [
    CommonModule,
    TableModule,
    MatDividerModule,
    FuseCardComponent,
    AsyncPipe,
    ProcessHeaderComponent,
    ToastrModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    OrderWidgetsSidebarComponent,
    ButtonModule,
  ],
  templateUrl: './shipment-details.component.html',
  styleUrl: './shipment-details.component.scss'
})
export class ShipmentDetailsComponent implements OnInit {

  constructor(
    private _router: Router,
    public header: ProcessHeaderService,
    private route: ActivatedRoute,
    private shipmentService: ShipmentService,
    private toaster: ToastrService,
    private sortService: SortService,
    private packingListService: PackingListService,
    private matDialog: MatDialog,
    private store: Store,
    private shippingLabelService: ShippingLabelService,
    private browserPrintingService: BrowserPrintingService,
    private translate: TranslateService,
    @Inject(LOCALE_ID) public locale: string,
  ) {
    this.store
      .select(getAuthState)
      .pipe(take(1))
      .subscribe(auth => {
        this.loggedUserId = auth['id'];
      });
  }

  expandedRows = {};
  orderInfoDescriptions: Description[] = [];
  shippingInfoDescriptions: Description[] = [];
  shipmentInfo: ShipmentInfoDto;
  products: ShipmentInfoItemDto[] = [];
  processProductConfig: ProcessProductDto;
  shippedInfoData: ShipmentCompleteInfoDto[] = [];
  loggedUserId: string;
  packedItems: FilledProductInfoDto[] = [];
  readonly validationType = ValidationType;

  get filledProductsCount() {
    return this.packedItems?.length;
  }

  get isProductComplete(): boolean {
    return this.shipmentInfo?.status === 'COMPLETED';
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
    this.fetchShipmentDetails();
  }

  fetchShipmentDetails(): void {
    this.shipmentService.getShipmentById(this.shipmentId, true).subscribe(result => {
      this.shipmentInfo = result.data?.getShipmentDetailsById;
      this.products = this.shipmentInfo?.items?.map(item => this.convertItemToProduct(item)) ?? [];
      this.getPackedItems();
      this.updateWidgets();
      if (this.isProductComplete) {
        this.getShippedProductsInfo();
      }
    });
  }

  private convertItemToProduct(item: ShipmentInfoItemDto): ShipmentInfoItemDto {
    return {
      id: item.id,
      quantity: item.quantity,
      comments: item.comments,
      productFamily: item.productFamily,
      bloodType: item.bloodType,
      packedItems: item.packedItems,
    };
  }

  getShippedProductsInfo(): void {
    this.shippedInfoData = [];
    const details = {
      completeDate: formatDate(this.shipmentInfo.completeDate, 'MM/dd/YYYY HH:mm', this.locale),
      completedByEmployee: this.shipmentInfo.completedByEmployeeId,
      quantity: this.packedItems?.length,
    };
    this.shippedInfoData.push(details);
  }

  getPackedItems(): void {
    this.packedItems = [];
    this.products.forEach(item => {
      if (item.packedItems?.length) {
        this.packedItems.push(...item.packedItems);
      }
    });
  }

  private updateWidgets() {
    this.orderInfoDescriptions = this.shipmentService.getOrderInfoDescriptions(this.shipmentInfo);
    this.shippingInfoDescriptions = this.shipmentService.getShippingInfoDescriptions(this.shipmentInfo);
  }

  fillProducts(item: ShipmentInfoItemDto): void {
    const url = `shipment/${this.shipmentId}/fill-products/${item.id}`;
    this._router.navigateByUrl(url);
  }

  getIcon(productFamily: string): string {
    return productFamily && this.processProductConfig?.properties[`icon.${productFamily}`]
      ? 'rsa:' + this.processProductConfig.properties[`icon.${productFamily}`]
      : 'rsa:product-plasma';
  }

  backToSearch(): void {
    this._router.navigateByUrl('/orders/search');
  }

  customSort(event: SortEvent) {
    this.sortService.customSort(event);
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
      .generate(this.shipmentInfo.id)
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
              }),
          });
          dialogRef.componentInstance.model$ = of(packingListLabel);
          return dialogRef.afterOpened();
        }),
        catchError(err => {
          this.toaster.error(ERROR_MESSAGE);
          throw err;
        })
      )
      .subscribe(() => {
        this.browserPrintingService.print('viewPackingListReport', { pageSize: DEFAULT_PAGE_SIZE });
        dialogRef?.close();
      });
  }

  viewShippingLabel(print?: boolean): void {
    let dialogRef: MatDialogRef<ViewShippingLabelComponent>;
    this.shippingLabelService
      .generate(this.shipmentInfo.id)
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
              }),
          });
          dialogRef.componentInstance.model$ = of(packingListLabel);
          return dialogRef.afterOpened();
        }),
        catchError(err => {
          this.toaster.error(ERROR_MESSAGE);
          throw err;
        })
      )
      .subscribe(() => {
        this.browserPrintingService.print('viewShippingLabelReport', { pageSize: DEFAULT_PAGE_SIZE });
        dialogRef?.close();
      });
  }

  completeShipment() {
    this.shipmentService.completeShipment(this.getValidateRuleDto()).subscribe({
      next: (response) => {
        const value = response.data?.completeShipment;
        const notifications = value.notifications;
        const url = value._links?.next;

        if (notifications?.length) {
          this.displayMessageFromNotificationDto(notifications[0]);
          if (url && notifications[0].notificationType === 'success') {
            this.fetchShipmentDetails();
          }
        }
      },
      error: (err) => {
        this.toaster.error(ERROR_MESSAGE);
        throw err;
      }
    }
    );
  }

  private getValidateRuleDto() {
    return {
      shipmentId: this.shipmentId,
      employeeId: this.loggedUserId,
    };
  }

  displayMessageFromNotificationDto(notification: NotificationDto) {
    this.toaster.show(
      this.translate.instant(notification.message),
      startCase(notification.notificationType),
      {},
      notification.notificationType
    );
  }
}
