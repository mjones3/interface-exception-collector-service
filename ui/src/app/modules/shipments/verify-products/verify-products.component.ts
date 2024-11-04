import { AsyncPipe, NgTemplateOutlet, PercentPipe } from '@angular/common';
import { Component, OnInit, ViewChild, computed, signal } from '@angular/core';
import { MatDivider } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import {
    NotificationTypeMap,
    ProcessHeaderComponent,
    ProcessHeaderService,
} from '@shared';
import { ToastrService } from 'ngx-toastr';
import { finalize, forkJoin, take, tap } from 'rxjs';
import { FuseCardComponent } from '../../../../@fuse';
import { getAuthState } from '../../../core/state/auth/auth.selectors';
import { ProgressBarComponent } from '../../../progress-bar/progress-bar.component';
import { ActionButtonComponent } from '../../../shared/components/action-button/action-button.component';
import { UnitNumberCardComponent } from '../../../shared/components/unit-number-card/unit-number-card.component';
import { ProductIconsService } from '../../../shared/services/product-icon.service';
import { VerifyProductResponseDTO } from '../graphql/verify-products/query-definitions/verify-products.graphql';
import {
    ShipmentDetailResponseDTO,
    ShipmentItemPackedDTO,
    VerifyFilledProductDto,
} from '../models/shipment-info.dto';
import { ShipmentService } from '../services/shipment.service';
import { EnterUnitNumberProductCodeComponent } from '../shared/enter-unit-number-product-code/enter-unit-number-product-code.component';
import { OrderWidgetsSidebarComponent } from '../shared/order-widgets-sidebar/order-widgets-sidebar.component';

@Component({
    selector: 'app-verify-products',
    standalone: true,
    imports: [
        AsyncPipe,
        ProcessHeaderComponent,
        ActionButtonComponent,
        FuseCardComponent,
        NgTemplateOutlet,
        OrderWidgetsSidebarComponent,
        PercentPipe,
        EnterUnitNumberProductCodeComponent,
        MatDivider,
        UnitNumberCardComponent,
        ProgressBarComponent,
    ],
    templateUrl: './verify-products.component.html',
})
export class VerifyProductsComponent implements OnInit {
    protected shipmentId: number;

    protected loggedUserIdSignal = signal<string>(null);
    protected shipmentSignal = signal<ShipmentDetailResponseDTO>(null);
    protected verificationSignal = signal<VerifyProductResponseDTO>(null);
    protected packedItemsComputed = computed(
        () => this.verificationSignal()?.packedItems ?? []
    );
    protected verifiedItemsComputed = computed(
        () => this.verificationSignal()?.verifiedItems ?? []
    );
    protected isAllPackItemsVerified = computed(
        () =>
            this.packedItemsComputed()?.length &&
            this.verifiedItemsComputed()?.length &&
            this.packedItemsComputed().length ===
                this.verifiedItemsComputed().length
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

    @ViewChild('enterUnitNumberProductCode')
    protected enterUnitNumberProductCode: EnterUnitNumberProductCodeComponent;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private store: Store,
        private shipmentService: ShipmentService,
        private productIconService: ProductIconsService,
        private toaster: ToastrService,
        protected header: ProcessHeaderService
    ) {
        this.shipmentId = Number(this.route.snapshot.params?.id);
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => this.loggedUserIdSignal.set(auth.id));
    }

    ngOnInit(): void {
        this.triggerFetchData();
    }

    triggerFetchData(): void {
        forkJoin({
            shipment: this.shipmentService.getShipmentById(this.shipmentId),
            verification:
                this.shipmentService.getShipmentVerificationDetailsById(
                    this.shipmentId
                ),
        }).subscribe(({ shipment, verification }) => {
            this.shipmentSignal.set(shipment.data?.getShipmentDetailsById);
            this.verificationSignal.set(
                verification.data?.getShipmentVerificationDetailsById
            );
        });
    }

    verifyItem(item: VerifyFilledProductDto): void {
        this.shipmentService
            .verifyItem({
                shipmentId: this.shipmentId,
                unitNumber: item.unitNumber,
                productCode: item.productCode,
                employeeId: this.loggedUserIdSignal(),
            })
            .pipe(
                tap((result) => {
                    const notifications =
                        result?.data?.verifyItem?.notifications;
                    notifications?.forEach((notification) => {
                        this.toaster.show(
                            notification.message,
                            null,
                            {},
                            NotificationTypeMap[notification.notificationType]
                                .type
                        );
                    });
                }),
                finalize(() => {
                    this.enterUnitNumberProductCode.productGroup.reset();
                    this.enterUnitNumberProductCode.unitNumberComponent.reset();
                    this.enterUnitNumberProductCode.enableProductCode();
                })
            )
            .subscribe((result) => {
                this.verificationSignal.set(
                    result.data?.verifyItem?.results?.results?.[0] ?? null
                );
                this.disableInputsIfAllPackItemsVerified();
            });
    }

    disableInputsIfAllPackItemsVerified(): void {
        if (this.isAllPackItemsVerified()) {
            this.enterUnitNumberProductCode.disableProductGroup();
        }
    }

    getItemIcon(item: ShipmentItemPackedDTO) {
        return this.productIconService.getIconByProductFamily(
            item.productFamily
        );
    }

    cancelButtonHandler(): void {
        this.router.navigateByUrl(
            `/shipment/${this.shipmentId}/shipment-details`
        );
    }
}
