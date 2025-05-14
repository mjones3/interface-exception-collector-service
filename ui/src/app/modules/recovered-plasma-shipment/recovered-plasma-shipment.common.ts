import { computed, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ToastrImplService } from '@shared';
import { Cookie } from 'app/shared/types/cookie.enum';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { CookieService } from 'ngx-cookie-service';
import { catchError, map, Observable, take, tap } from 'rxjs';
import { getAuthState } from '../../core/state/auth/auth.selectors';
import { ProductIconsService } from '../../shared/services/product-icon.service';
import { consumeUseCaseNotifications } from '../../shared/utils/notification.handling';
import { FindShipmentRequestDTO } from './graphql/query-definitions/shipmentDetails.graphql';
import { RecoveredPlasmaShipmentResponseDTO } from './models/recovered-plasma.dto';
import { RecoveredPlasmaService } from './services/recovered-plasma.service';

export class RecoveredPlasmaShipmentCommon {

    routeIdComputed = computed(() => Number(this.route?.snapshot?.params?.id));
    employeeIdSignal = signal<string>(null);
    shipmentDetailsSignal = signal<RecoveredPlasmaShipmentResponseDTO>(null);
    shipmentIdComputed = computed<number>(
        () => this.shipmentDetailsSignal()?.id
    );
    locationCodeComputed = computed(() =>
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

    getItemIcon(productType: string ) {
        return this.productIconService.getIconByProductFamily(productType);
    }

    loadRecoveredPlasmaShippingDetails(
        shipmentId?: number
    ): Observable<RecoveredPlasmaShipmentResponseDTO> {
        return this.recoveredPlasmaService
            .getShipmentById(this.prepareShipmentRequest(shipmentId))
            .pipe(
                catchError((error: ApolloError) => {
                    handleApolloError(this.toastr, error);
                }),
                tap((response) =>
                    consumeUseCaseNotifications(
                        this.toastr,
                        response.data?.findShipmentById?.notifications
                    )
                ),
                map((response) => {
                    const { data } = response.data.findShipmentById;
                    this.shipmentDetailsSignal.set(data);
                    return data;
                })
            );
    }

    protected prepareShipmentRequest(
        shipmentId?: number
    ): FindShipmentRequestDTO {
        return {
            employeeId: this.employeeIdSignal(),
            locationCode: this.locationCodeComputed(),
            shipmentId: shipmentId ?? this.routeIdComputed(),
        };
    }
}
