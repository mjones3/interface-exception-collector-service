import { HttpResponse } from '@angular/common/http';
import { Component, OnInit, ViewChild } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import {
  Description,
  FacilityService,
  FilledProductInfoDto,
  ProcessHeaderService,
  ProcessProductDto,
  ShipmentInfoDto,
  ShipmentInfoItemDto,
  ShipmentService,
  TranslateInterpolationPipe,
  ValidationType,
  VerifyFilledProduct,
  VerifyProduct,
} from '@rsa/commons';
import { getAuthState } from '@rsa/global-data';
import { startCase } from 'lodash';
import { ToastrService } from 'ngx-toastr';
import { catchError, finalize, take } from 'rxjs/operators';
import { EnterUnitNumberProductCodeComponent } from '../enter-unit-number-product-code/enter-unit-number-product-code.component';

@Component({
  selector: 'rsa-fill-products',
  templateUrl: './fill-products.component.html',
  styleUrls: ['./fill-products.component.scss'],
})
export class FillProductsComponent implements OnInit {
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
    private translateInterpolationPipe: TranslateInterpolationPipe,
    private facilityService: FacilityService,
    private store: Store,
    private _router: Router
  ) {
    store
      .select(getAuthState)
      .pipe(take(1))
      .subscribe(auth => {
        this.loggedUserId = auth['id'];
      });
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
      this.shipmentProduct = this.shipmentInfo?.items?.find(item => item.id === +this.productId);
      (this.prodIcon = this.getIcon(this.shipmentProduct?.productFamily)), this.setProdInfo();
      this.updateWidgets();
    });
  }
  private updateWidgets() {
    this.orderInfoDescriptions = this.shipmentService.getOrderInfoDescriptions(this.shipmentInfo);
    this.shippingInfoDescriptions = this.shipmentService.getShippingInfoDescriptions(this.shipmentInfo);
  }

  getIcon(productFamily: string): string {
    return productFamily && this.processProductConfig?.properties[`icon.${productFamily}`]
      ? 'rsa:' + this.processProductConfig.properties[`icon.${productFamily}`]
      : 'rsa:product-whole-blood';
  }

  private setProdInfo() {
    this.prodInfoDescriptions = [
      {
        label: 'product-family.label',
        value: this.shipmentProduct?.productFamily,
      },
      {
        label: 'blood-type.label',
        value: this.shipmentProduct?.bloodType,
      },
      {
        label: 'product-comments.label',
        value: this.shipmentProduct.comments,
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

  unitNumberProductCodeSelected(item: VerifyFilledProduct) {
    return this.shipmentService
      .verifyShipmentProduct(this.getVerifyUnitNumberProductCodeDto(item))
      .pipe(
        catchError(err => {
          this.loading = false;
          this.toaster.error('something-went-wrong.label');
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
        const ruleResult = response?.body;
        if (ruleResult) {
          this.loading = false;
          const notification = ruleResult.notifications?.length ? ruleResult.notifications[0] : null;
          if (ruleResult.ruleCode === 'OK') {
            const result = ruleResult?.results?.results[0] || ruleResult?.results[0];
            if (result) {
              this.filledProductsData = result.packedItems;
              this.filledProductsData = [...this.filledProductsData];
            }
          }
          if (notification) {
            this.toaster.show(
              this.translateInterpolationPipe.transform(notification.message, []),
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
          }
        }
      });
  }

  disableFilUnitNumberAndProductCode(): void {
    this.productSelection.disableProductGroup();
  }

  private getVerifyUnitNumberProductCodeDto(item: VerifyFilledProduct): VerifyProduct {
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
