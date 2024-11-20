import { AsyncPipe, PercentPipe } from '@angular/common';
import { Component, computed, OnInit, ViewChild } from '@angular/core';
import { MatDivider } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { finalize, switchMap, tap } from 'rxjs';
import { FuseCardComponent } from '../../../../@fuse';
import { FuseConfirmationService } from '../../../../@fuse/services/confirmation';
import { ProgressBarComponent } from '../../../progress-bar/progress-bar.component';
import { ScanUnitNumberProductCodeComponent } from '../../../scan-unit-number-product-code/scan-unit-number-product-code.component';
import { ActionButtonComponent } from '../../../shared/components/action-button/action-button.component';
import { GlobalMessageComponent } from '../../../shared/components/global-message/global-message.component';
import { UnitNumberCardComponent } from '../../../shared/components/unit-number-card/unit-number-card.component';
import { RuleResponseDTO } from '../../../shared/models/rule.model';
import { ProductIconsService } from '../../../shared/services/product-icon.service';
import { consumeNotifications } from '../../../shared/utils/notification.handling';
import { RemoveProductResponseDTO } from '../graphql/verify-products/query-definitions/verify-products.graphql';
import {
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
        protected router: Router,
        protected store: Store,
        protected shipmentService: ShipmentService,
        protected confirmationService: FuseConfirmationService,
        protected productIconService: ProductIconsService,
        protected toaster: ToastrImplService,
        protected header: ProcessHeaderService
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
                finalize(() =>
                    this.scanUnitNumberProductCode.resetUnitProductGroup()
                ),
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
            this.openAcknowledgmentMessageDialog(
                removeItem?.results?.results?.[0]?.removedItem
                    ?.ineligibleMessage
            );
        }
        if (removeItem.ruleCode === '400 BAD_REQUEST') {
            consumeNotifications(this.toaster, removeItem?.notifications);
        }
    }

    openAcknowledgmentMessageDialog(message: string): void {
        this.confirmationService.open({
            title: 'Acknowledgment Message',
            message: message,
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

    disableInputsIfAllRemovableItemsWereRemoved(): void {
        if (this.notificationDetailsSignal()?.toBeRemovedItems?.length === 0) {
            this.scanUnitNumberProductCode?.disableUnitProductGroup();
        }
    }
}
