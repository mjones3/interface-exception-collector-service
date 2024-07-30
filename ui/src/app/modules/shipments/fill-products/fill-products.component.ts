import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder, FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseCardComponent } from '@fuse/components/card';
import { Store } from '@ngrx/store';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { startCase } from 'lodash-es';
import { ToastrService } from 'ngx-toastr';
import { TableModule } from 'primeng/table';
import { catchError, finalize, take } from 'rxjs';
import { ERROR_MESSAGE } from '../../../../../public/i18n/common-labels';
import { getAuthState } from '../../../core/state/auth/auth.selectors';
import { DescriptionCardComponent } from '../../../shared/components/information-card/description-card.component';
import { ProcessHeaderComponent } from '../../../shared/components/process-header/process-header.component';
import { Description } from '../../../shared/models/description.model';
import { ProcessProductDto } from '../../../shared/models/process-product.dto';
import { ValidationType } from '../../../shared/pipes/validation.pipe';
import { FacilityService } from '../../../shared/services';
import { ProcessHeaderService } from '../../../shared/services/process-header.service';
import { FilledProductInfoDto, ShipmentInfoDto, ShipmentInfoItemDto, VerifyFilledProductDto, VerifyProductDto } from '../models/shipment-info.dto';
import { ShipmentService } from '../services/shipment.service';
import { EnterUnitNumberProductCodeComponent } from '../shared/enter-unit-number-product-code/enter-unit-number-product-code.component';
import { OrderWidgetsSidebarComponent } from '../shared/order-widgets-sidebar/order-widgets-sidebar.component';

@Component({
  selector: 'app-fill-products',
  standalone: true,
  imports: [
    OrderWidgetsSidebarComponent,
    DescriptionCardComponent, 
    ProcessHeaderComponent,
    EnterUnitNumberProductCodeComponent,
    FuseCardComponent,
    CommonModule,
    MatProgressBarModule,
    TableModule,
    MatDividerModule,
    MatButtonModule,
    FormsModule,
    TranslateModule
  ],
  templateUrl: './fill-products.component.html',
  styleUrl: './fill-products.component.scss',
  providers: [ShipmentService],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FillProductsComponent implements OnInit{

  filledProductsData: FilledProductInfoDto[] = [];
  orderInfoDescriptions: Description[] = [];
  shippingInfoDescriptions: Description[] = [];
  prodInfoDescriptions: Description[] = [];
  shipmentInfo: ShipmentInfoDto;
  shipmentProduct: ShipmentInfoItemDto;
  prodIcon: string;
  loading = false;
  unitNumberFocus = true;
  productCodeFocus = false;
  loggedUserId: string;
  processProductConfig: ProcessProductDto;

  readonly validationType = ValidationType;

  @ViewChild('productSelection') productSelection: EnterUnitNumberProductCodeComponent;

  constructor(
    public header: ProcessHeaderService,
    private toaster: ToastrService,
    protected fb: FormBuilder,
    private route: ActivatedRoute,
    private shipmentService: ShipmentService,
    private translateService: TranslateService,
    private facilityService: FacilityService,
    private store: Store,
    private _router: Router,
    private cd: ChangeDetectorRef
  ){
    this.store
      .select(getAuthState)
      .pipe(take(1))
      .subscribe(auth => {
        this.loggedUserId = auth['id'];
      });
  }

  ngOnInit(){
    this.fetchShipmentDetails();
  }


  fetchShipmentDetails(): void {
    this.shipmentService.getShipmentById(this.shipmentId, true).subscribe(result => {
      this.shipmentInfo = result.data?.getShipmentDetailsById;
      this.shipmentProduct = this.shipmentInfo?.items?.find(item => item.id === this.productId);
      this.filledProductsData = this.shipmentProduct?.packedItems;
      
      this.prodIcon = this.getIcon(this.shipmentProduct?.productFamily);
      this.setProdInfo();
      this.updateWidgets();
      this.cd.detectChanges()
    });
  }
  private updateWidgets() {
    this.orderInfoDescriptions = this.shipmentService.getOrderInfoDescriptions(this.shipmentInfo);
    this.shippingInfoDescriptions = this.shipmentService.getShippingInfoDescriptions(this.shipmentInfo);
  }

  getIcon(productFamily: string): string {
    return productFamily && this.processProductConfig?.properties[`icon.${productFamily}`]
      ? 'rsa:' + this.processProductConfig.properties[`icon.${productFamily}`]
      : 'rsa:product-plasma';
  }

  private setProdInfo() {
    this.prodInfoDescriptions = [
      {
        label: 'Product Family',
        value: this.shipmentProduct?.productFamily,
      },
      {
        label: 'Blood Type',
        value: this.shipmentProduct?.bloodType,
      },
      {
        label: 'Product Commnets',
        value: this.shipmentProduct?.comments,
      },
    ];
  }

  get quantity() {
    return this.shipmentProduct?.quantity;
  }

  get shipmentId() {
    return this.route.snapshot.params?.id;
  }

  get productId(): string {
    return this.route.snapshot.params?.productId;
  }

  backToShipmentDetails() {
    this._router.navigateByUrl(`/shipment/${this.shipmentId}/shipment-details`);
  }

  unitNumberProductCodeSelected(item: VerifyFilledProductDto) {
    return this.shipmentService
      .verifyShipmentProduct(this.getVerifyUnitNumberProductCodeDto(item))
      .pipe(
        catchError(err => {
          this.loading = false;
          this.toaster.error(ERROR_MESSAGE);
          if (this.productSelection) {
            this.productSelection.resetProductFormGroup();
          }
          throw err;
        }),
        finalize(() => {
          this.loading = false;
          this.unitNumberFocus = true;
        })
      )
      .subscribe(response => {
        if (this.productSelection) {
          this.productSelection.resetProductFormGroup();
        }
        const ruleResult = response?.data.packItem;
        if (ruleResult) {
          this.loading = false;
          const notification = ruleResult.notifications?.length ? ruleResult.notifications[0] : null;
          if (ruleResult.ruleCode === '200 OK') {
            const result = ruleResult?.results?.results[0] || ruleResult?.results[0];
            if (result) {
              this.filledProductsData = result.packedItems;
              this.filledProductsData = [...this.filledProductsData];
              this.productSelection.productGroup.reset();
              this.productSelection.enableVisualInspection()
            }
          }
          if (notification) {
            this.toaster.show(
              this.translateService.instant(notification.message),
              startCase(notification.notificationType),
              {},
              notification.notificationType
            );

            if (
              notification.message === 'product-criteria-quantity-exceeded.error' ||
              this.quantity === this.filledProductsData.length
            ) {
              this.disableFilUnitNumberAndProductCode();
            }
            else if(notification.statusCode !==200){
              this.productSelection.productGroup.reset();
              this.productSelection.enableVisualInspection()
            }
          }
        }
      });
  }

  disableFilUnitNumberAndProductCode(): void {
    this.productSelection.disableProductGroup();
  }

  private getVerifyUnitNumberProductCodeDto(item: VerifyFilledProductDto): VerifyProductDto {
    return {
      shipmentItemId: +this.productId,
      unitNumber: item.unitNumber,
      productCode: item.productCode,
      locationCode: this.facilityService.getFacilityId(),
      employeeId: this.loggedUserId,
      visualInspection: item.visualInspection.toUpperCase(),
    };
  }
}
