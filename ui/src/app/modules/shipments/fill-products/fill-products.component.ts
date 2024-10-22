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

import { MatDialog } from '@angular/material/dialog';
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
import { ActionButtonComponent } from 'app/shared/components/action-button/action-button.component';
import { UnitNumberCardComponent } from 'app/shared/components/unit-number-card/unit-number-card.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { Cookie } from 'app/shared/types/cookie.enum';
import { CookieService } from 'ngx-cookie-service';
import { TableModule } from 'primeng/table';
import { catchError, finalize, take } from 'rxjs';
import {
    RecordUnsatisfactoryVisualInspectionComponent,
    RecordUnsatisfactoryVisualInspectionData,
    RecordUnsatisfactoryVisualInspectionResult,
} from '../../../shared/components/record-unsatisfactory-visual-inspection/record-unsatisfactory-visual-inspection.component';
import { DiscardRequestDTO } from '../../../shared/models/discard.model';
import { InventoryDTO } from '../../../shared/models/inventory.model';
import { ProductFamilyMap } from '../../../shared/models/product-family.model';
import { ReasonDTO } from '../../../shared/models/reason.dto';
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
        UnitNumberCardComponent,
        ActionButtonComponent,
        OrderWidgetsSidebarComponent,
    ],
    templateUrl: './fill-products.component.html',
    styleUrl: './fill-products.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class FillProductsComponent implements OnInit {
    filledProductsData: ShipmentItemPackedDTO[] = [];
    prodInfoDescriptions: Description[] = [];
    selectedProducts: VerifyFilledProductDto[] = [];
    shipmentInfo: ShipmentDetailResponseDTO;
    shipmentProduct: ShipmentItemResponseDTO;
    prodIcon: string;
    loading = false;
    unitNumberFocus = true;
    productCodeFocus = false;
    loggedUserId: string;
    processProductConfig: ProcessProductModel;
    showCheckDigit = true;
    showVisualInspection = false;

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
        private discardService: DiscardService,
        private productIconService: ProductIconsService,
        private recordUnsatisfactoryVisualInspectionDialog: MatDialog
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
                this.showCheckDigit = this.shipmentInfo.checkDigitActive;
                this.showVisualInspection =
                    this.shipmentInfo.visualInspectionActive;
                this.shipmentProduct = this.shipmentInfo?.items?.find(
                    (item) => item.id === this.productId
                );
                this.filledProductsData = this.shipmentProduct?.packedItems;

                this.cd.detectChanges();
                this.productSelection.buildFormGroup();
            });
    }

    get productFamily() {
        return ProductFamilyMap[this.shipmentProduct?.productFamily];
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
                    this.productSelection.enableVisualInspection();
                    this.productSelection.enableProductCode();
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
                    if (ruleResult.ruleCode === '200 OK') {
                        const result =
                            ruleResult?.results?.results[0] ||
                            ruleResult?.results[0];
                        if (result) {
                            this.filledProductsData = [...result.packedItems];
                            this.productSelection.productGroup.reset();
                            this.productSelection.enableVisualInspection();
                            this.productSelection.enableProductCode();
                        }
                    }

                    const notifications: NotificationDto[] =
                        ruleResult && ruleResult.notifications
                            ? [...ruleResult.notifications]
                            : [];
                    if (notifications?.length) {
                        const infoNotifications = this.pullOutNotifications(
                            notifications,
                            { notificationType: 'INFO' }
                        );
                        const inventory = ruleResult?.results?.inventory?.[0];
                        if (infoNotifications?.length) {
                            if (
                                infoNotifications.find(
                                    (n) => n.action === 'TRIGGER_DISCARD'
                                )
                            ) {
                                return this.triggerDiscard(
                                    infoNotifications,
                                    inventory
                                );
                            } else {
                                return this.openAcknowledgmentMessageDialog(
                                    infoNotifications
                                );
                            }
                        }

                        const unsatisfactoryVisualInspection =
                            this.pullOutNotifications(notifications, {
                                notificationType: 'WARN',
                                name: 'PRODUCT_CRITERIA_VISUAL_INSPECTION_ERROR',
                            })?.[0];
                        if (unsatisfactoryVisualInspection) {
                            const reasons = ruleResult?.results?.reasons;
                            return this.showUnsatisfactoryVisualInspectionDialog(
                                reasons,
                                unsatisfactoryVisualInspection.message,
                                inventory
                            );
                        }

                        this.displayMessageFromNotificationDto(notifications);
                        notifications.forEach((notification) => {
                            if (
                                notification.name ===
                                    'PRODUCT_CRITERIA_QUANTITY_ERROR' ||
                                this.quantity === this.filledProductsData.length
                            ) {
                                this.disableFillUnitNumberAndProductCode();
                            } else if (notification.statusCode !== 200) {
                                this.productSelection.productGroup.reset();
                                this.productSelection.enableVisualInspection();
                                this.productSelection.enableProductCode();
                            }
                        });
                    }
                }
            });
    }

    private pullOutNotifications(
        notifications: NotificationDto[],
        sample: Partial<
            Pick<NotificationDto, 'notificationType' | 'name' | 'action'>
        >
    ): NotificationDto[] {
        // Filtering notifications according to sample
        const filteredNotifications = notifications.filter(
            (n) =>
                (sample?.notificationType
                    ? n.notificationType === sample?.notificationType
                    : true) &&
                (sample?.name ? n.name === sample?.name : true) &&
                (sample?.action ? n.action === sample?.action : true)
        );

        // Removing filtered notifications from original array
        for (const notification of filteredNotifications) {
            const i = notifications.findIndex(
                (n) =>
                    n.notificationType === notification.notificationType &&
                    n.name === notification.name &&
                    n.action === notification.action
            );
            notifications.splice(i, 1);
        }
        return filteredNotifications;
    }

    private showUnsatisfactoryVisualInspectionDialog(
        reasons: ReasonDTO[],
        message: string,
        inventory: InventoryDTO
    ): void {
        if (!reasons?.length) {
            this.toaster.error(
                'Unable to record unsatisfactory visual inspection. No reasons provided by the system. Contact support.'
            );
            return;
        }
        this.recordUnsatisfactoryVisualInspectionDialog
            .open<
                RecordUnsatisfactoryVisualInspectionComponent,
                RecordUnsatisfactoryVisualInspectionData,
                RecordUnsatisfactoryVisualInspectionResult
            >(RecordUnsatisfactoryVisualInspectionComponent, {
                disableClose: true,
                width: '42rem',
                data: {
                    reasons,
                    message,
                    inventory,
                },
            })
            .afterClosed()
            .subscribe((result) => {
                if (result) {
                    this.discardService
                        .discardProduct(
                            this.getDiscardRequestDto(
                                result.inventory,
                                result.reason.reasonKey,
                                result.comments
                            )
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
                                this.productSelection.productGroup.reset();
                                this.productSelection.enableVisualInspection();
                                return this.openAcknowledgmentMessageDialog([
                                    {
                                        statusCode: 400,
                                        notificationType: 'INFO',
                                        code: 400,
                                        message: result.message,
                                    },
                                ]);
                            } else {
                                this.showDiscardSystemError();
                            }
                        });
                } else {
                    this.productSelection.productGroup.reset();
                    this.productSelection.enableVisualInspection();
                    this.productSelection.enableProductCode();
                }
            });
    }

    disableFillUnitNumberAndProductCode(): void {
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
            visualInspection: this.showVisualInspection
                ? item.visualInspection.toUpperCase()
                : null,
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

    openAcknowledgmentMessageDialog(notifications: NotificationDto[]): void {
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

    private triggerDiscard(
        triggerDiscardNotifications: NotificationDto[],
        inventory: InventoryDTO
    ): void {
        for (const triggerDiscardNotification of triggerDiscardNotifications) {
            this.discardService
                .discardProduct(
                    this.getDiscardRequestDto(
                        inventory,
                        triggerDiscardNotification.reason
                    )
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
                        return this.openAcknowledgmentMessageDialog(
                            triggerDiscardNotifications
                        );
                    } else {
                        this.showDiscardSystemError();
                    }
                });
        }
    }

    private getDiscardRequestDto(
        inventory: InventoryDTO,
        reason: string,
        comments?: string
    ): DiscardRequestDTO {
        return {
            unitNumber: inventory.unitNumber,
            productCode: inventory.productCode,
            locationCode: this.cookieService.get(Cookie.XFacility),
            employeeId: this.loggedUserId,
            triggeredBy: 'SHIPPING',
            reasonDescriptionKey: reason,
            productFamily: inventory.productFamily,
            productShortDescription: inventory.productDescription,
            comments: comments,
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

    getIcon() {
        return this.productIconService.getIconByProductFamily(
            this.shipmentProduct?.productFamily
        );
    }

    toggleProduct(product: VerifyFilledProductDto) {
        if (this.selectedProducts.includes(product)) {
            const index = this.selectedProducts.findIndex(
                (filterProduct) =>
                    filterProduct.unitNumber === product.unitNumber &&
                    filterProduct.productCode === product.productCode
            );
            this.selectedProducts.splice(index, 1);
        } else {
            this.selectedProducts.push(product);
        }
    }
}
