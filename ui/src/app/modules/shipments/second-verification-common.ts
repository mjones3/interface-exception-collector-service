import { computed, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Store } from '@ngrx/store';
import { ToastrImplService } from '@shared';
import { forkJoin, take, tap } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { getAuthState } from '../../core/state/auth/auth.selectors';
import { ProductIconsService } from '../../shared/services/product-icon.service';
import handleApolloError from '../../shared/utils/apollo-error-handling';
import { consumeNotifications } from '../../shared/utils/notification.handling';
import {
    RemoveProductResponseDTO,
    VerifyProductResponseDTO,
} from './graphql/verify-products/query-definitions/verify-products.graphql';
import { ShipmentDetailResponseDTO } from './models/shipment-info.dto';
import { ShipmentService } from './services/shipment.service';

export class SecondVerificationCommon {
    protected currentRouteComputed = computed(() => this.router.url);
    protected shipmentIdComputed = computed(() =>
        Number(this.route.snapshot.params?.id)
    );
    protected loggedUserIdSignal = signal<string>(null);
    protected shipmentSignal = signal<ShipmentDetailResponseDTO>(null);
    protected verificationSignal = signal<VerifyProductResponseDTO>(null);
    protected notificationDetailsSignal =
        signal<RemoveProductResponseDTO>(null);
    protected packedItemsComputed = computed(
        () => this.verificationSignal()?.packedItems ?? []
    );
    protected verifiedItemsComputed = computed(
        () => this.verificationSignal()?.verifiedItems ?? []
    );
    protected isAllPackItemsVerified = computed(() => {
        return (
            this.packedItemsComputed()?.length &&
            this.verifiedItemsComputed()?.length &&
            this.packedItemsComputed().length ===
                this.verifiedItemsComputed().length
        );
    });

    constructor(
        protected route: ActivatedRoute,
        protected router: Router,
        protected store: Store,
        protected shipmentService: ShipmentService,
        protected toaster: ToastrImplService,
        protected productIconService: ProductIconsService
    ) {
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => this.loggedUserIdSignal.set(auth.id));
    }

    protected triggerFetchData() {
        return forkJoin({
            shipment: this.shipmentService.getShipmentById(
                this.shipmentIdComputed()
            ),
            verification:
                this.shipmentService.getShipmentVerificationDetailsById(
                    this.shipmentIdComputed()
                ),
            notificationDetails:
                this.shipmentService.getNotificationDetailsByShipmentId(
                    this.shipmentIdComputed()
                ),
        });
    }

    protected completeShipment() {
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

    getItemIcon(item: { productFamily?: string }) {
        return this.productIconService.getIconByProductFamily(
            item.productFamily
        );
    }

    async handleNavigation(url: string): Promise<boolean> {
        return await this.router.navigateByUrl(url);
    }

    async goBackToShipmentDetails(): Promise<boolean> {
        return await this.handleNavigation(
            `/shipment/${this.shipmentIdComputed()}/shipment-details`
        );
    }
}
