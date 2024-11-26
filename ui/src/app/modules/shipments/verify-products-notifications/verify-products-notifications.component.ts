import { AsyncPipe, PercentPipe } from '@angular/common';
import { Component, OnInit, ViewChild, computed } from '@angular/core';
import { MatDivider } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { ConfirmationAcknowledgmentService } from 'app/shared/services/confirmation-acknowledgment.service';
import { CookieService } from 'ngx-cookie-service';
import { catchError, finalize, switchMap, tap } from 'rxjs';
import { FuseCardComponent } from '../../../../@fuse';
import { FuseConfirmationService } from '../../../../@fuse/services/confirmation';
import { ProgressBarComponent } from '../../../progress-bar/progress-bar.component';
import { ScanUnitNumberProductCodeComponent } from '../../../scan-unit-number-product-code/scan-unit-number-product-code.component';
import { ActionButtonComponent } from '../../../shared/components/action-button/action-button.component';
import { GlobalMessageComponent } from '../../../shared/components/global-message/global-message.component';
import { UnitNumberCardComponent } from '../../../shared/components/unit-number-card/unit-number-card.component';
import { DiscardRequestDTO } from '../../../shared/models/discard.model';
import { RuleResponseDTO } from '../../../shared/models/rule.model';
import { DiscardService } from '../../../shared/services/discard.service';
import { ProductIconsService } from '../../../shared/services/product-icon.service';
import { Cookie } from '../../../shared/types/cookie.enum';
import { consumeNotifications } from '../../../shared/utils/notification.handling';
import { RemoveProductResponseDTO } from '../graphql/verify-products/query-definitions/verify-products.graphql';
import {
    ShipmentItemPackedDTO,
    ShipmentItemResponseDTO,
    VerifyFilledProductDto,
} from '../models/shipment-info.dto';
import { SecondVerificationCommon } from '../second-verification-common';
import { ShipmentService } from '../services/shipment.service';
import { OrderWidgetsSidebarComponent } from '../shared/order-widgets-sidebar/order-widgets-sidebar.component';
import { VerifyProductsNavbarComponent } from '../verify-products-navbar/verify-products-navbar.component';

@Component({
    selector: 'app-verify-products-notifications',
    standalone: true,
    imports: [
        ActionButtonComponent,
        AsyncPipe,
        FuseCardComponent,
        MatDivider,
        OrderWidgetsSidebarComponent,
        PercentPipe,
        ProcessHeaderComponent,
        ProgressBarComponent,
        ScanUnitNumberProductCodeComponent,
        UnitNumberCardComponent,
        VerifyProductsNavbarComponent,
        GlobalMessageComponent,
    ],
    templateUrl: './verify-products-notifications.component.html',
})
export class VerifyProductsNotificationsComponent
    extends SecondVerificationCommon
    implements OnInit
{
    protected verifyProductsRouteComputed = computed(
        () => `/shipment/${this.route.snapshot.params?.id}/verify-products`
    );
    protected shipmentItemsTotalProductsComputed = computed(() =>
        this.shipmentSignal()?.items?.reduce<number>(
            (previousValue: number, currentValue: ShipmentItemResponseDTO) =>
                previousValue + +currentValue?.quantity,
            0
        )
    );
    protected toBeRemovedItemsComputed = computed(
        () => this.notificationDetailsSignal()?.toBeRemovedItems ?? []
    );
    protected removedItemsComputed = computed(
        () => this.notificationDetailsSignal()?.removedItems ?? []
    );
    protected removedItemsPercentage = computed(() => {
        if (
            (this.toBeRemovedItemsComputed()?.length ?? 0) +
                (this.removedItemsComputed()?.length ?? 0) ===
            0
        ) {
            return 0;
        }
        return (
            this.removedItemsComputed()?.length /
            (this.toBeRemovedItemsComputed()?.length +
                this.removedItemsComputed()?.length)
        );
    });
    protected shipmentIdComputed = computed(() =>
        Number(this.route.snapshot.params?.id)
    );

    @ViewChild('scanUnitNumberProductCode')
    protected scanUnitNumberProductCode: ScanUnitNumberProductCodeComponent;

    constructor(
        protected route: ActivatedRoute,
        private confirmationAcknowledgmentService: ConfirmationAcknowledgmentService,
        protected router: Router,
        protected store: Store,
        protected shipmentService: ShipmentService,
        protected confirmationService: FuseConfirmationService,
        protected productIconService: ProductIconsService,
        protected toaster: ToastrImplService,
        protected header: ProcessHeaderService,
        private discardService: DiscardService,
        private cookieService: CookieService
    ) {
        super(
            route,
            router,
            store,
            shipmentService,
            toaster,
            productIconService
        );
    }

    ngOnInit(): void {
        this.subscribeTriggerFetchData();
    }

    subscribeTriggerFetchData(): void {
        super
            .triggerFetchData()
            .pipe(
                finalize(() => {
                    this.disableInputsIfAllRemovableItemsWereRemoved();
                })
            )
            .subscribe(({ shipment, verification, notificationDetails }) => {
                this.shipmentSignal.set(shipment.data?.getShipmentDetailsById);
                this.verificationSignal.set(
                    verification.data?.getShipmentVerificationDetailsById
                );
                this.notificationDetailsSignal.set(
                    notificationDetails.data.getNotificationDetailsByShipmentId
                );
            });
    }

    removeItem(item: VerifyFilledProductDto): void {
        this.shipmentService
            .removeItem({
                shipmentId: this.shipmentIdComputed(),
                unitNumber: item.unitNumber,
                productCode: item.productCode,
                employeeId: this.loggedUserIdSignal(),
            })
            .pipe(
                tap((result) =>
                    this.consumeAcknowledgeNotification(result.data.removeItem)
                ),
                finalize(() => {
                    this.scanUnitNumberProductCode.resetUnitProductGroup();
                }),
                switchMap((result) => {
                    this.notificationDetailsSignal.set(
                        result.data?.removeItem?.results?.results?.[0] ?? null
                    );
                    this.disableInputsIfAllRemovableItemsWereRemoved();

                    return this.shipmentService.getShipmentVerificationDetailsById(
                        this.shipmentIdComputed()
                    );
                })
            )
            .subscribe((verification) => {
                this.verificationSignal.set(
                    verification.data?.getShipmentVerificationDetailsById
                );
            });
    }

    consumeAcknowledgeNotification(
        removeItem: RuleResponseDTO<{ results: RemoveProductResponseDTO[] }>
    ): void {
        if (removeItem.ruleCode === '200 OK') {
            const removedItem = removeItem?.results?.results?.[0]?.removedItem;

            if (removedItem?.ineligibleAction === 'TRIGGER_DISCARD') {
                return this.triggerDiscard(removedItem);
            } else {
                this.openAcknowledgmentMessageDialog(
                    removedItem?.ineligibleMessage,
                    removedItem.details
                );
            }
        }

        if (removeItem.ruleCode === '400 BAD_REQUEST') {
            consumeNotifications(this.toaster, removeItem?.notifications, () =>
                this.scanUnitNumberProductCode.focusOnUnitNumber()
            );
        }

        if (removeItem._links?.next) {
            this.handleNavigation(removeItem._links?.next);
        }
    }

    openAcknowledgmentMessageDialog(message: string, details: string[]): void {
        this.confirmationAcknowledgmentService.notificationConfirmation(
            message,
            details
        );
    }

    disableInputsIfAllRemovableItemsWereRemoved(): void {
        if (this.notificationDetailsSignal()?.toBeRemovedItems?.length === 0) {
            this.scanUnitNumberProductCode?.disableUnitProductGroup();
        }
    }

    private triggerDiscard(itemPackedDTO: ShipmentItemPackedDTO): void {
        this.discardService
            .discardProduct(this.getDiscardRequestDto(itemPackedDTO))
            .pipe(
                catchError((err) => {
                    this.showDiscardSystemError();
                    throw err;
                })
            )
            .subscribe((response) => {
                const data = response?.data?.discardProduct;
                if (data) {
                    return this.openAcknowledgmentMessageDialog(
                        itemPackedDTO.ineligibleMessage,
                        null
                    );
                } else {
                    this.showDiscardSystemError();
                }
            });
    }

    private getDiscardRequestDto(
        itemPackedDTO: ShipmentItemPackedDTO
    ): DiscardRequestDTO {
        return {
            unitNumber: itemPackedDTO.unitNumber,
            productCode: itemPackedDTO.productCode,
            locationCode: this.cookieService.get(Cookie.XFacility),
            employeeId: this.loggedUserIdSignal(),
            triggeredBy: 'SHIPPING',
            reasonDescriptionKey: itemPackedDTO.ineligibleReason,
            productFamily: itemPackedDTO.productFamily,
            productShortDescription: itemPackedDTO.productDescription,
            comments: '',
        };
    }

    private showDiscardSystemError() {
        consumeNotifications(this.toaster, [
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
