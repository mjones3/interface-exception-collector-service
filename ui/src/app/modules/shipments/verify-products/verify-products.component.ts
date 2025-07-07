import { AsyncPipe, PercentPipe } from '@angular/common';
import { Component, OnInit, ViewChild, computed } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatDivider } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import {
    NotificationDto,
    NotificationTypeMap,
    ProcessHeaderComponent,
    ProcessHeaderService
} from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { NotificationComponent } from 'app/shared/components/notification/notification.component';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { finalize, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { FuseCardComponent } from '../../../../@fuse';
import { FuseConfirmationService } from '../../../../@fuse/services/confirmation';
import { GlobalMessageComponent } from '../../../shared/components/global-message/global-message.component';
import { ProgressBarComponent } from '../../../shared/components/progress-bar/progress-bar.component';
import { UnitNumberCardComponent } from '../../../shared/components/unit-number-card/unit-number-card.component';
import { ProductCategoryMap } from '../../../shared/models/product-category.model';
import { ProductIconsService } from '../../../shared/services/product-icon.service';
import handleApolloError from '../../../shared/utils/apollo-error-handling';
import { consumeNotifications } from '../../../shared/utils/notification.handling';
import { CancelSecondVerificationRequest } from '../graphql/verify-products/query-definitions/verify-products.graphql';
import { VerifyFilledProductDto } from '../models/shipment-info.dto';
import { SecondVerificationCommon } from '../second-verification-common';
import { ShipmentService } from '../services/shipment.service';
import { OrderWidgetsSidebarComponent } from '../shared/order-widgets-sidebar/order-widgets-sidebar.component';
import { VerifyProductsNavbarComponent } from '../verify-products-navbar/verify-products-navbar.component';
import { ToastrService } from 'ngx-toastr';

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
        protected route: ActivatedRoute,
        protected router: Router,
        protected store: Store,
        protected shipmentService: ShipmentService,
        protected productIconService: ProductIconsService,
        protected toaster: ToastrService,
        protected header: ProcessHeaderService,
        protected matDialog: MatDialog,
        protected fuseConfirmationService: FuseConfirmationService
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

    async handleNavigation(url: string): Promise<boolean> {
        return await this.router.navigateByUrl(url);
    }

    cancelButtonHandler(): void {
        const request: CancelSecondVerificationRequest = {
            shipmentId: this.shipmentIdComputed(),
            employeeId: this.loggedUserIdSignal(),
        };
        this.shipmentService
            .cancelSecondVerification(request)
            .pipe(catchError((e) => handleApolloError(this.toaster, e)))
            .subscribe(async (result) => {
                const confirmationNotification =
                    result.data?.cancelSecondVerification?.notifications?.find(
                        (n) => n.notificationType === 'CONFIRMATION'
                    );
                if (confirmationNotification) {
                    this.scanUnitNumberProductCode?.disableUnitProductGroup();
                    return this.fuseConfirmationService
                        .open({
                            title: 'Cancel Confirmation',
                            message: confirmationNotification.message,
                            dismissible: false,
                            icon: {
                                show: false,
                            },
                            actions: {
                                confirm: {
                                    label: 'Yes',
                                    class: 'bg-red-700 text-white font-bold',
                                },
                                cancel: {
                                    class: 'font-bold',
                                },
                            },
                        })
                        .afterClosed()
                        .subscribe((result) => {
                            this.scanUnitNumberProductCode?.resetUnitProductGroup();
                            if (result === 'confirmed') {
                                this.confirmCancel();
                            }
                        });
                }

                consumeNotifications(
                    this.toaster,
                    result?.data?.cancelSecondVerification?.notifications
                );
                if (
                    result.data?.cancelSecondVerification?.ruleCode === '200 OK'
                ) {
                    return await this.handleNavigation(
                        result.data?.cancelSecondVerification?._links?.next
                    );
                }
            });
    }

    confirmCancel(): void {
        const request: CancelSecondVerificationRequest = {
            shipmentId: this.shipmentIdComputed(),
            employeeId: this.loggedUserIdSignal(),
        };
        this.shipmentService
            .confirmCancelSecondVerification(request)
            .pipe(
                catchError((e) => handleApolloError(this.toaster, e)),
                tap((result) =>
                    consumeNotifications(
                        this.toaster,
                        result?.data?.confirmCancelSecondVerification
                            ?.notifications
                    )
                )
            )
            .subscribe(async (result) => {
                if (
                    result.data?.confirmCancelSecondVerification?.ruleCode ===
                    '200 OK'
                ) {
                    await this.handleNavigation(
                        result.data.confirmCancelSecondVerification._links.next
                    );
                }
            });
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
                            this.scanUnitNumberProductCode.disableUnitProductGroup();
                            this.matDialog
                                .open(NotificationComponent, {
                                    data: {
                                        data: result?.data?.completeShipment,
                                    },
                                    disableClose: true,
                                })
                                .afterClosed()
                                .subscribe(() => {
                                    this.handleNavigation(
                                        `/shipment/${this.shipmentIdComputed()}/verify-products/notifications`
                                    );
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
            this.toaster
                .show(
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
                )
                .onTap.subscribe(() =>
                    this.scanUnitNumberProductCode.focusOnUnitNumber()
                );
        });
    }

    protected readonly ProductCategoryMap = ProductCategoryMap;
}
