import { AsyncPipe, NgTemplateOutlet, PercentPipe } from '@angular/common';
import { Component, OnInit, ViewChild, computed, signal } from '@angular/core';
import { MatDivider } from '@angular/material/divider';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ScanUnitNumberProductCodeComponent } from 'app/scan-unit-number-product-code/scan-unit-number-product-code.component';
import { ToastrService } from 'ngx-toastr';
import { finalize, forkJoin, take, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { FuseCardComponent } from '../../../../@fuse';
import { getAuthState } from '../../../core/state/auth/auth.selectors';
import { ProgressBarComponent } from '../../../progress-bar/progress-bar.component';
import { ActionButtonComponent } from '../../../shared/components/action-button/action-button.component';
import { UnitNumberCardComponent } from '../../../shared/components/unit-number-card/unit-number-card.component';
import { ProductIconsService } from '../../../shared/services/product-icon.service';
import handleApolloError from '../../../shared/utils/apollo-error-handling';
import { consumeNotifications } from '../../../shared/utils/notification.handling';
import { VerifyProductResponseDTO } from '../graphql/verify-products/query-definitions/verify-products.graphql';
import {
    ShipmentDetailResponseDTO,
    ShipmentItemPackedDTO,
    VerifyFilledProductDto,
} from '../models/shipment-info.dto';
import { ShipmentService } from '../services/shipment.service';
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
        MatDivider,
        UnitNumberCardComponent,
        ProgressBarComponent,
        ScanUnitNumberProductCodeComponent,
    ],
    templateUrl: './verify-products.component.html',
})
export class VerifyProductsComponent implements OnInit {
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
    protected shipmentIdComputed = computed(() =>
        Number(this.route.snapshot.params?.id)
    );

    @ViewChild('scanUnitNumberProductCode')
    protected scanUnitNumberProductCode: ScanUnitNumberProductCodeComponent;

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private store: Store,
        private shipmentService: ShipmentService,
        private productIconService: ProductIconsService,
        private toaster: ToastrService,
        protected header: ProcessHeaderService
    ) {
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
            shipment: this.shipmentService.getShipmentById(
                this.shipmentIdComputed()
            ),
            verification:
                this.shipmentService.getShipmentVerificationDetailsById(
                    this.shipmentIdComputed()
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
                tap((result) =>
                    consumeNotifications(
                        this.toaster,
                        result?.data?.completeShipment.notifications
                    )
                ),
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
}
