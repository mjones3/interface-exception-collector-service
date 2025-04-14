import { computed, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ToastrImplService } from '@shared';
import { Cookie } from 'app/shared/types/cookie.enum';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { CookieService } from 'ngx-cookie-service';
import { catchError, take } from 'rxjs';
import { getAuthState } from '../../core/state/auth/auth.selectors';
import { ProductIconsService } from '../../shared/services/product-icon.service';
import { FindShipmentRequestDTO } from './graphql/query-definitions/shipmentDetails.graphql';
import { RecoveredPlasmaShipmentResponseDTO } from './models/recovered-plasma.dto';
import { RecoveredPlasmaService } from './services/recovered-plasma.service';

export class RecoveredPlasmaShipmentCommon {
    protected cartonsRouteComputed = computed(() => this.router.url);
    protected shipmentIdComputed = computed(() =>
        Number(this.route.snapshot.params?.id)
    );
    protected employeeIdSignal = signal<string>(null);
    protected shipmentDetailsSignal =
        signal<RecoveredPlasmaShipmentResponseDTO>(null);
    protected shipmentIdSignal = signal<number>(null);
    protected locationCodeComputed = computed(() =>
        this.cookieService.get(Cookie.XFacility)
    );

    constructor(
        protected route: ActivatedRoute,
        protected router: Router,
        protected store: Store,
        protected recoveredPlasmaService: RecoveredPlasmaService,
        protected toastr: ToastrImplService,
        protected productIconService: ProductIconsService,
        protected cookieService: CookieService
    ) {
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => this.employeeIdSignal.set(auth.id));
    }

    getItemIcon(item: { productFamily?: string }) {
        return this.productIconService.getIconByProductFamily(
            item.productFamily
        );
    }

    getShipmentId() {
        const shipmentId = parseInt(this.route.snapshot.params?.id);
        return this.shipmentIdSignal.set(shipmentId);
    }

    fetchRecoveredPlasmaShippingDetails() {
        return this.recoveredPlasmaService
            .getShipmentById(this.prepareShipmentRequest())
            .pipe(
                catchError((error: ApolloError) => {
                    handleApolloError(this.toastr, error);
                })
            )
            .subscribe({
                next: (response) => {
                    this.shipmentDetailsSignal.set(
                        response.data?.findShipmentById?.data
                    );
                },
            });
    }

    protected prepareShipmentRequest(): FindShipmentRequestDTO {
        return {
            employeeId: this.employeeIdSignal(),
            locationCode: this.locationCodeComputed(),
            shipmentId: this.shipmentIdComputed(),
        };
    }
}
