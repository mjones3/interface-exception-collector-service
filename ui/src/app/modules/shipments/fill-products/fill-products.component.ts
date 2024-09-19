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

import { FuseConfirmationService } from '@fuse/services/confirmation/public-api';
import {
    Description,
    DescriptionCardComponent,
    NotificationDto,
    NotificationTypeMap,
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
import { DiscardRequestDTO } from '../../../shared/models/discard.model';
import { InventoryDTO } from '../../../shared/models/inventory.model';
import { ProductFamilyMap } from '../../../shared/models/product-family.model';
import { RuleResponseDTO } from '../../../shared/models/rule.model';
import { DiscardService } from '../../../shared/services/discard.service';
import {
    ShipmentDetailResponseDTO,
    ShipmentItemPackedDTO,
    ShipmentItemResponseDTO,
    VerifyFilledProductDto,
    VerifyProductDTO,
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
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FillProductsComponent implements OnInit {
    filledProductsData: ShipmentItemPackedDTO[] = [];
    prodInfoDescriptions: Description[] = [];
    shipmentInfo: ShipmentDetailResponseDTO;
    shipmentProduct: ShipmentItemResponseDTO;
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
        private cd: ChangeDetectorRef,
        private confirmationService: FuseConfirmationService,
        private discardService: DiscardService
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
            .getShipmentById(this.shipmentId)
            .subscribe((result) => {
                this.shipmentInfo = result.data?.getShipmentDetailsById;
                this.shipmentProduct = this.shipmentInfo?.items?.find(
                    (item) => item.id === this.productId
                );
                this.filledProductsData = this.shipmentProduct?.packedItems;

                this.setProdInfo();
                this.cd.detectChanges();
            });
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
                    const notifications = ruleResult.notifications
                        ? ruleResult.notifications
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

                    if (notifications) {
                        if (
                            notifications.find(
                                (notification) =>
                                    'INFO' === notification.notificationType
                            )
                        ) {
                            return this.triggerDiscard(ruleResult);
                        } else {
                            this.displayMessageFromNotificationDto(
                                ruleResult.notifications
                            );
                        }
                        notifications.forEach((notification) => {
                            if (
                                notification.name ===
                                    'PRODUCT_CRITERIA_QUANTITY_ERROR' ||
                                this.quantity === this.filledProductsData.length
                            ) {
                                this.disableFilUnitNumberAndProductCode();
                            } else if (notification.statusCode !== 200) {
                                this.productSelection.productGroup.reset();
                                this.productSelection.enableVisualInspection();
                            }
                        });
                    }
                }
            });
    }

    disableFilUnitNumberAndProductCode(): void {
        this.productSelection.disableProductGroup();
    }

    private getVerifyUnitNumberProductCodeDto(
        item: VerifyFilledProductDto
    ): VerifyProductDTO {
        return {
            shipmentItemId: +this.productId,
            unitNumber: item.unitNumber,
            productCode: item.productCode,
            locationCode: this.cookieService.get(Cookie.XFacility),
            employeeId: this.loggedUserId,
            visualInspection: item.visualInspection.toUpperCase(),
        };
    }

    displayMessageFromNotificationDto(notifications: NotificationDto[]) {
        notifications.forEach((notification) => {
            const notificationType =
                NotificationTypeMap[notification.notificationType];
            this.toaster.show(
                this.translateService.instant(notification.message),
                notificationType.title,
                {
                    ...(notificationType.timeOut
                        ? { timeOut: notificationType.timeOut }
                        : {}),
                    ...(notification.notificationType === 'SYSTEM'
                        ? { timeOut: 0 }
                        : {}), // Overrides timeout definition for SYSTEM notifications
                },
                notificationType.type
            );
        });
    }

    openConfirmationDialog(notifications: NotificationDto[]): void {
        this.confirmationService.open({
            title: 'Acknowledgment Message',
            message: notifications
                .map((notification) => notification.message)
                .join('<br/>'),
            dismissible: false,
            icon: {
                show: false,
            },
            actions: {
                confirm: {
                    label: 'Confirm',
                    show: true,
                    class: 'bg-violet-300 text-violet-700 font-bold',
                },
                cancel: {
                    show: false,
                },
            },
        });
    }

    private triggerDiscard(ruleResponse: RuleResponseDTO) {
        const inventoryData: InventoryDTO = ruleResponse?.results?.inventory[0];
        const triggers = ruleResponse.notifications.filter(
            (notification) => 'TRIGGER_DISCARD' === notification.action
        );
        if (triggers.length) {
            triggers.forEach((notification) => {
                return this.discardService
                    .discardProduct(
                        this.getDiscardRequestDto(inventoryData, notification)
                    )
                    .pipe(
                        catchError((err) => {
                            this.showDiscardSystemError();
                            throw err;
                        }),
                        finalize(() => {
                            this.loading = false;
                            this.unitNumberFocus = true;
                        })
                    )
                    .subscribe((response) => {
                        const data = response?.data?.discardProduct;
                        if (data) {
                            return this.openConfirmationDialog(
                                ruleResponse.notifications
                            );
                        } else {
                            this.showDiscardSystemError();
                        }
                    });
            });
        } else {
            return this.openConfirmationDialog(ruleResponse.notifications);
        }
    }

    private getDiscardRequestDto(
        inventory: InventoryDTO,
        notification: NotificationDto
    ): DiscardRequestDTO {
        return {
            unitNumber: inventory.unitNumber,
            productCode: inventory.productCode,
            locationCode: this.cookieService.get(Cookie.XFacility),
            employeeId: this.loggedUserId,
            triggeredBy: 'SHIPPING',
            reasonDescriptionKey: notification.reason,
            productFamily: inventory.productFamily,
            productShortDescription: inventory.productDescription,
            comments: '',
        };
    }

    private showDiscardSystemError() {
        this.loading = false;
        this.displayMessageFromNotificationDto([
            {
                statusCode: 400,
                notificationType: 'SYSTEM',
                code: 400,
                message:
                    'Product has not been discarded in the system. Contact Support.',
            },
        ]);
    }
}
