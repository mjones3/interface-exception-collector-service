import { AsyncPipe, NgTemplateOutlet, PercentPipe } from '@angular/common';
import { Component, OnInit, ViewChild, computed, signal } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { AsyncPipe, PercentPipe } from '@angular/common';
import { MatDivider } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import {
    NotificationDto,
    NotificationTypeMap,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { ScanUnitNumberProductCodeComponent } from 'app/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { NotificationComponent } from 'app/shared/components/notification/notification.component';
import { finalize, forkJoin, take, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ToastrService } from 'ngx-toastr';
import { finalize, tap } from 'rxjs';
import { FuseCardComponent } from '../../../../@fuse';
import { ProgressBarComponent } from '../../../progress-bar/progress-bar.component';
import { ActionButtonComponent } from '../../../shared/components/action-button/action-button.component';
import { GlobalMessageComponent } from '../../../shared/components/global-message/global-message.component';
import { UnitNumberCardComponent } from '../../../shared/components/unit-number-card/unit-number-card.component';
import { ProductIconsService } from '../../../shared/services/product-icon.service';
import { consumeNotifications } from '../../../shared/utils/notification.handling';
import { VerifyFilledProductDto } from '../models/shipment-info.dto';
import { SecondVerificationCommon } from '../second-verification-common';
import { ShipmentService } from '../services/shipment.service';
import { OrderWidgetsSidebarComponent } from '../shared/order-widgets-sidebar/order-widgets-sidebar.component';
import { VerifyProductsNavbarComponent } from '../verify-products-navbar/verify-products-navbar.component';

@Component({
    selector: 'app-verify-products',
    standalone: true,
    imports: [
        AsyncPipe,
        ProcessHeaderComponent,
        ActionButtonComponent,
        FuseCardComponent,
        OrderWidgetsSidebarComponent,
        PercentPipe,
        MatDivider,
        UnitNumberCardComponent,
        ProgressBarComponent,
        ScanUnitNumberProductCodeComponent,
        VerifyProductsNavbarComponent,
        GlobalMessageComponent,
        NotificationComponent,
    ],
    templateUrl: './verify-products.component.html',
})
export class VerifyProductsComponent
    extends SecondVerificationCommon
    implements OnInit
{
    protected verifyProductsNotificationsRouteComputed = computed(
        () =>
            `/shipment/${this.route.snapshot.params?.id}/verify-products/notifications`
    );
    protected verifiedItemsPercentage = computed(() => {
        if (
            (this.packedItemsComputed()?.length ?? 0) <= 0 ||
            (this.verifiedItemsComputed()?.length ?? 0) <= 0
        ) {
            return 0;
        }
        return (
            this.verifiedItemsComputed()?.length /
            this.packedItemsComputed()?.length
        );
    });
    protected toBeRemovedItemsComputed = computed(
        () => this.notificationDetailsSignal()?.toBeRemovedItems ?? []
    );

    @ViewChild('scanUnitNumberProductCode')
    protected scanUnitNumberProductCode: ScanUnitNumberProductCodeComponent;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private store: Store,
        private shipmentService: ShipmentService,
        private productIconService: ProductIconsService,
        protected header: ProcessHeaderService,
        private matDialog: MatDialog,
        private toaster: ToastrImplService
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

    verifyItem(item: VerifyFilledProductDto): void {
        this.shipmentService
            .verifyItem({
                shipmentId: this.shipmentIdComputed(),
                unitNumber: item.unitNumber,
                productCode: item.productCode,
                employeeId: this.loggedUserIdSignal(),
            })
            .pipe(
                tap((result) =>
                    consumeNotifications(
                        this.toaster,
                        result?.data?.verifyItem?.notifications,
                        () => {
                            this.scanUnitNumberProductCode.focusOnUnitNumber();
                        }
                    )
                ),
                finalize(() =>
                    this.scanUnitNumberProductCode.resetUnitProductGroup()
                )
            )
            .subscribe((result) => {
                this.verificationSignal.set(
                    result.data?.verifyItem?.results?.results?.[0] ?? null
                );
                this.disableInputsIfAllPackItemsVerified();
                if (result?.data?.verifyItem?.ruleCode === '200 OK') {
                    this.scanUnitNumberProductCode.focusOnUnitNumber();
                }
            });
    }

    disableInputsIfAllPackItemsVerified(): void {
        if (this.isAllPackItemsVerified()) {
            this.scanUnitNumberProductCode.disableUnitProductGroup();
        }
    }

    getItemIcon(item: ShipmentItemPackedDTO) {
        return this.productIconService.getIconByProductFamily(
            item.productFamily
        );
    }

    async handleNavigation(url: string): Promise<boolean> {
        return await this.router.navigateByUrl(url);
    }

    async cancelButtonHandler(): Promise<boolean> {
        return await this.handleNavigation(
            `/shipment/${this.shipmentIdComputed()}/shipment-details`
        );
    }

    completeShipment() {
        this.shipmentService
            .completeShipment({
                shipmentId: this.shipmentIdComputed(),
                employeeId: this.loggedUserIdSignal(),
            })
            .pipe(
                tap((result) => {
                    if (result?.data?.completeShipment?.notifications?.length) {
                        if (
                            result?.data?.completeShipment?.notifications?.find(
                                (n) => n.notificationType === 'CONFIRMATION'
                            )
                        ) {
                            this.matDialog.open(NotificationComponent, {
                                data: { data: result?.data?.completeShipment },
                                disableClose: true,
                            });
                        } else {
                            const notificationDto: NotificationDto[] =
                                result?.data?.completeShipment?.notifications;
                            this.displayMessageFromNotificationDto(
                                notificationDto
                            );
                        }
                    }
                }),
                catchError((e) => handleApolloError(this.toaster, e))
            )
            .subscribe((response) => {
                if (response.data?.completeShipment?._links?.next) {
                    this.handleNavigation(
                        response.data?.completeShipment?._links?.next
                    );
                }
            });
    }

    displayMessageFromNotificationDto(notifications: NotificationDto[]) {
        notifications.forEach((notification) => {
            const notificationType =
                NotificationTypeMap[notification.notificationType];
            this.toaster.show(
                notification.message,
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
}
