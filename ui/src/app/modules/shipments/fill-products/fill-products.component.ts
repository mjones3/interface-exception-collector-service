import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    OnInit,
    ViewChild,
} from '@angular/core';
import { FormBuilder, FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { Store } from '@ngrx/store';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

import {
    Description,
    DescriptionCardComponent,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ProcessProductModel,
    ToastrImplService,
} from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { Cookie } from 'app/shared/types/cookie.enum';
import { CookieService } from 'ngx-cookie-service';
import { TableModule } from 'primeng/table';
import { catchError, finalize, take } from 'rxjs';
import { ProductFamilyMap } from '../../../shared/models/product-family.model';
import {
    FilledProductInfoDto,
    ShipmentInfoDto,
    ShipmentInfoItemDto,
    VerifyFilledProductDto,
    VerifyProductDto,
} from '../models/shipment-info.dto';
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
        TranslateModule,
    ],
    templateUrl: './fill-products.component.html',
    styleUrl: './fill-products.component.scss',
    providers: [ShipmentService],
    changeDetection: ChangeDetectionStrategy.OnPush,
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
    processProductConfig: ProcessProductModel;

    @ViewChild('productSelection')
    productSelection: EnterUnitNumberProductCodeComponent;

    constructor(
        public header: ProcessHeaderService,
        private toaster: ToastrImplService,
        protected fb: FormBuilder,
        private route: ActivatedRoute,
        private shipmentService: ShipmentService,
        private translateService: TranslateService,
        private cookieService: CookieService,
        private store: Store,
        private _router: Router,
        private cd: ChangeDetectorRef
    ) {
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => {
                this.loggedUserId = auth['id'];
            });
    }

    ngOnInit() {
        this.fetchShipmentDetails();
    }

    fetchShipmentDetails(): void {
        this.shipmentService
            .getShipmentById(this.shipmentId, true)
            .subscribe((result) => {
                this.shipmentInfo = result.data?.getShipmentDetailsById;
                this.shipmentProduct = this.shipmentInfo?.items?.find(
                    (item) => item.id === this.productId
                );
                this.filledProductsData = this.shipmentProduct?.packedItems;

                this.setProdInfo();
                this.updateWidgets();
                this.cd.detectChanges();
            });
    }
    private updateWidgets() {
        this.orderInfoDescriptions =
            this.shipmentService.getOrderInfoDescriptions(this.shipmentInfo);
        this.shippingInfoDescriptions =
            this.shipmentService.getShippingInfoDescriptions(this.shipmentInfo);
    }

    private setProdInfo() {
        this.prodInfoDescriptions = [
            {
                label: 'Product Family',
                value: ProductFamilyMap[this.shipmentProduct?.productFamily],
            },
            {
                label: 'Blood Type',
                value: this.shipmentProduct?.bloodType,
            },
            {
                label: 'Product Comments',
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
        this._router.navigateByUrl(
            `/shipment/${this.shipmentId}/shipment-details`
        );
    }

    unitNumberProductCodeSelected(item: VerifyFilledProductDto) {
        return this.shipmentService
            .verifyShipmentProduct(this.getVerifyUnitNumberProductCodeDto(item))
            .pipe(
                catchError((err) => {
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
            .subscribe((response) => {
                if (this.productSelection) {
                    this.productSelection.resetProductFormGroup();
                }
                const ruleResult = response?.data.packItem;
                if (ruleResult) {
                    this.loading = false;
                    const notification = ruleResult.notifications?.length
                        ? ruleResult.notifications[0]
                        : null;
                    if (ruleResult.ruleCode === '200 OK') {
                        const result =
                            ruleResult?.results?.results[0] ||
                            ruleResult?.results[0];
                        if (result) {
                            this.filledProductsData = result.packedItems;
                            this.filledProductsData = [
                                ...this.filledProductsData,
                            ];
                            this.productSelection.productGroup.reset();
                            this.productSelection.enableVisualInspection();
                        }
                    }
                    if (notification) {
                        this.toaster.show(
                            this.translateService.instant(notification.message),
                            null,
                            {},
                            notification.notificationType
                        );

                        if (
                            notification.message ===
                                'Product Criteria Quantity Exceeded' ||
                            this.quantity === this.filledProductsData.length
                        ) {
                            this.disableFilUnitNumberAndProductCode();
                        } else if (notification.statusCode !== 200) {
                            this.productSelection.productGroup.reset();
                            this.productSelection.enableVisualInspection();
                        }
                    }
                }
            });
    }

    disableFilUnitNumberAndProductCode(): void {
        this.productSelection.disableProductGroup();
    }

    private getVerifyUnitNumberProductCodeDto(
        item: VerifyFilledProductDto
    ): VerifyProductDto {
        return {
            shipmentItemId: +this.productId,
            unitNumber: item.unitNumber,
            productCode: item.productCode,
            locationCode: this.cookieService.get(Cookie.XFacility),
            employeeId: this.loggedUserId,
            visualInspection: item.visualInspection.toUpperCase(),
        };
    }
}
