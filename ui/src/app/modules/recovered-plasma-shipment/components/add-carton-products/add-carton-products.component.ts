import { AsyncPipe, CommonModule } from '@angular/common';
import { Component, ViewChild, computed, signal } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { MatDividerModule } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { Store } from '@ngrx/store';
import {
    NotificationTypeMap,
    ProcessHeaderComponent,
    ProcessHeaderService,
    ScanUnitNumberCheckDigitComponent,
    ToastrImplService,
} from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { ScanUnitNumberProductCodeComponent } from 'app/shared/components/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { UnitNumberCardComponent } from 'app/shared/components/unit-number-card/unit-number-card.component';
import { DiscardRequestDTO } from 'app/shared/models/discard.model';
import { UseCaseNotificationDTO } from 'app/shared/models/use-case-response.dto';
import { ConfirmationAcknowledgmentService } from 'app/shared/services/confirmation-acknowledgment.service';
import { DiscardService } from 'app/shared/services/discard.service';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { Subscription, catchError } from 'rxjs';
import { PackCartonItemsDTO } from '../../graphql/mutation-definitions/pack-items.graphql';
import {
    CartonDTO,
    CartonPackedItemResponseDTO,
    RecoveredPlasmaShipmentResponseDTO,
} from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { ShippingCartonInformationCardComponent } from '../../shared/shipping-carton-information-card/shipping-carton-information-card.component';
import { ShippingInformationCardComponent } from '../../shared/shipping-information-card/shipping-information-card.component';

@Component({
    selector: 'biopro-add-carton-products',
    standalone: true,
    imports: [
        AsyncPipe,
        ProcessHeaderComponent,
        CommonModule,
        MatDividerModule,
        ScanUnitNumberCheckDigitComponent,
        UnitNumberCardComponent,
        ShippingInformationCardComponent,
        ShippingCartonInformationCardComponent,
        ActionButtonComponent,
        FuseCardComponent,
        ScanUnitNumberProductCodeComponent,
    ],
    templateUrl: './add-carton-products.component.html',
})
export class AddCartonProductsComponent extends RecoveredPlasmaShipmentCommon {
    recoveredPlasmaProduct: FormGroup;
    formValueChange: Subscription;
    findShipmentById: RecoveredPlasmaShipmentResponseDTO;
    packedProductsData: PackCartonItemsDTO[] = [];
    totalProductsComputed = computed(
        () => this.shipmentDetailsSignal()?.totalProducts
    );
    cartonDetailsSignal = signal<CartonDTO>(null);

    @ViewChild('scanUnitNumberProductCode')
    protected scanUnitNumberProductCode: ScanUnitNumberProductCodeComponent;

    constructor(
        public header: ProcessHeaderService,
        protected fb: FormBuilder,
        protected router: Router,
        protected route: ActivatedRoute,
        protected store: Store,
        protected toastr: ToastrImplService,
        protected productIconService: ProductIconsService,
        protected recoveredPlasmaService: RecoveredPlasmaService,
        protected cookieService: CookieService,
        private discardService: DiscardService,
        private confirmationAcknowledgmentService: ConfirmationAcknowledgmentService
    ) {
        super(
            route,
            router,
            store,
            recoveredPlasmaService,
            toastr,
            productIconService,
            cookieService
        );
    }

    // FIXME
    // ngOnInit(): void {
    // this.loadRecoveredPlasmaShippingCartonDetails(this.routeIdComputed())
    //     .pipe(
    //         switchMap((carton) =>
    //             this.loadRecoveredPlasmaShippingDetails(carton.shipmentId)
    //         )
    //     )
    //     .subscribe();
    // }

    // FIXME
    // loadRecoveredPlasmaShippingCartonDetails(
    //     id: number
    // ): Observable<CartonDTO> {
    //     return this.recoveredPlasmaService.getCartonById(id).pipe(
    //         catchError((error: ApolloError) => {
    //             handleApolloError(this.toastr, error);
    //         }),
    //         tap((response) =>
    //             consumeUseCaseNotifications(
    //                 this.toastr,
    //                 response.data?.findCartonById?.notifications
    //             )
    //         ),
    //         map((response) => {
    //             const { data } = response.data.findCartonById;
    //             this.cartonDetailsSignal.set(data);
    //             return data;
    //         })
    //     );
    // }

    backToShipment() {
        this.router.navigateByUrl(
            `/recovered-plasma/${this.shipmentIdComputed()}/shipment-details`
        );
    }

    enterAndVerifyProduct(item: PackCartonItemsDTO) {
        return this.recoveredPlasmaService
            .addCartonProducts(this.getCartonProductRequest(item))
            .pipe(
                catchError((err) => {
                    if (this.scanUnitNumberProductCode) {
                        this.scanUnitNumberProductCode.resetUnitProductGroup();
                    }
                    this.toastr.error(ERROR_MESSAGE);
                    throw err;
                })
            )
            .subscribe({
                next: (response) => {
                    const productResult = response?.data?.packCartonItem;
                    if (productResult) {
                        if (productResult?.data) {
                            if (Array.isArray(productResult.data)) {
                                this.packedProductsData = productResult.data;
                            }
                            this.scanUnitNumberProductCode.resetUnitProductGroup();
                        }
                        const notifications: UseCaseNotificationDTO[] =
                            productResult.notifications;
                        if (notifications.length > 0) {
                            this.scanUnitNumberProductCode.resetUnitProductGroup();
                            const infoNotifications = notifications.filter(
                                (notification) => notification.type === 'INFO'
                            );
                            const otherNotifications = notifications.filter(
                                (notification) => notification.type !== 'INFO'
                            );
                            infoNotifications.forEach((infoNotification) => {
                                const packItem = productResult?.data;
                                if (
                                    infoNotification.action ===
                                    'TRIGGER_DISCARD'
                                ) {
                                    return this.triggerDiscard(
                                        infoNotifications[0],
                                        packItem
                                    );
                                } else {
                                    return this.openAcknowledgmentMessageDialog(
                                        infoNotifications[0]
                                    );
                                }
                            });
                            this.displayMessageFromNotificationDto(
                                otherNotifications
                            );
                            otherNotifications.forEach((notification) => {
                                if (
                                    notification.message ===
                                    'PRODUCT_CRITERIA_QUANTITY_ERROR'
                                ) {
                                    this.disableInputsIfMaxCartonProduct();
                                } else {
                                    this.scanUnitNumberProductCode.resetUnitProductGroup();
                                    this.scanUnitNumberProductCode.enableProductCode();
                                }
                            });
                        }
                    }
                },
            });
    }

    disableInputsIfMaxCartonProduct(): void {
        if (this.packedProductsData.length === this.totalProductsComputed()) {
            this.scanUnitNumberProductCode.disableUnitProductGroup();
        }
    }

    private triggerDiscard(
        triggerDiscardNotifications: UseCaseNotificationDTO,
        packItem: CartonPackedItemResponseDTO
    ): void {
        this.discardService
            .discardProduct(
                this.getDiscardRequestDto(
                    packItem,
                    triggerDiscardNotifications.reason
                )
            )
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
                        triggerDiscardNotifications
                    );
                } else {
                    this.showDiscardSystemError();
                }
            });
    }

    private showDiscardSystemError() {
        this.displayMessageFromNotificationDto([
            {
                type: 'SYSTEM',
                message:
                    'Product has not been discarded in the system. Contact Support.',
            },
        ]);
    }

    displayMessageFromNotificationDto(notifications: UseCaseNotificationDTO[]) {
        notifications.forEach((notification) => {
            const notificationType = NotificationTypeMap[notification.type];
            this.toastr.show(
                notification.message,
                notificationType.title,
                {
                    ...(notificationType.timeOut
                        ? { timeOut: notificationType.timeOut }
                        : {}),
                    ...(notification.type === 'SYSTEM' ? { timeOut: 0 } : {}), // Overrides timeout definition for SYSTEM notifications
                },
                notificationType.type
            );
        });
    }

    private getDiscardRequestDto(
        packItem: CartonPackedItemResponseDTO,
        reason: string,
        comments?: string
    ): DiscardRequestDTO {
        return {
            unitNumber: packItem?.unitNumber,
            productCode: packItem?.productCode,
            locationCode: this.locationCodeComputed(),
            employeeId: this.employeeIdSignal(),
            triggeredBy: 'SHIPPING',
            reasonDescriptionKey: reason,
            productFamily: packItem?.productType,
            productShortDescription: packItem?.productDescription,
            comments: comments,
        };
    }

    openAcknowledgmentMessageDialog(
        notification: UseCaseNotificationDTO
    ): void {
        const message = notification.message;
        const details = notification.details;
        this.confirmationAcknowledgmentService.notificationConfirmation(
            message,
            details,
            () => {
                this.scanUnitNumberProductCode.resetUnitProductGroup();
                this.scanUnitNumberProductCode.enableProductCode();
            }
        );
    }

    private getCartonProductRequest(
        item: PackCartonItemsDTO
    ): PackCartonItemsDTO {
        return {
            cartonId: this.routeIdComputed(),
            unitNumber: item.unitNumber,
            productCode: item.productCode,
            locationCode: this.locationCodeComputed(),
            employeeId: this.employeeIdSignal(),
        };
    }
}
