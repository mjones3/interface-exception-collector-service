import { AsyncPipe } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ToastrService } from 'ngx-toastr';
import {
    ProcessHeaderComponent,
    ProcessHeaderService, ToastrImplService,
} from '@shared';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { take } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { catchError, map, take, tap } from 'rxjs';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { FindShipmentRequestDTO } from '../../graphql/query-definitions/shipmentDetails.graphql';
import { RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { ShippingInformationCardComponent } from '../../shared/shipping-information-card/shipping-information-card.component';
import { RecoveredPlasmaShipmentDetailsNavbarComponent } from '../recovered-plasma-shipment-details-navbar/recovered-plasma-shipment-details-navbar.component';

@Component({
    selector: 'biopro-recovered-plasma-shipping-details',
    standalone: true,
    imports: [
        ProcessHeaderComponent,
        ActionButtonComponent,
        AsyncPipe,
        ShippingInformationCardComponent,
        BasicButtonComponent,
        RecoveredPlasmaShipmentDetailsNavbarComponent,
    ],
    templateUrl: './recovered-plasma-shipping-details.component.html',
})
export class RecoveredPlasmaShippingDetailsComponent
    extends RecoveredPlasmaShipmentCommon
    implements OnInit
{
    findShipmentById: RecoveredPlasmaShipmentResponseDTO;
    employeeId: string;
    protected cartonsRouteComputed = computed(() => this.router.url);

    constructor(
        public header: ProcessHeaderService,
        protected store: Store,
        protected route: ActivatedRoute,
        protected router: Router,
        protected toastr: ToastrImplService,
        protected recoveredPlasmaService: RecoveredPlasmaService,
        protected cookieService: CookieService,
        protected productIconService: ProductIconsService,
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
        this.setEmployeeId();
    }

    ngOnInit(): void {
        this.subscribeTriggerFetchData();
    }

    subscribeTriggerFetchData(): void {
        super.fetchRecoveredPlasmaShippingDetails();
    }

    private setEmployeeId() {
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => {
                this.employeeId = auth['id'];
            });
    }

    get shipmentId(): number {
        return parseInt(this.route.snapshot.params?.id);
    }

    backToSearch() {
        this.router.navigate(['/recovered-plasma']);
    }

    addCarton(): void {
        const shipmentId = +this.route.snapshot.params?.id;
        this.recoveredPlasmaService
            .createCarton({ shipmentId, employeeId: this.employeeId })
            .pipe(
                catchError((error: ApolloError) => {
                    handleApolloError(this.toaster, error);
                }),
                tap((response) =>
                    consumeUseCaseNotifications(
                        this.toaster,
                        response.data?.createCarton?.notifications
                    )
                ),
                map((response) => response.data?.createCarton?.data)
            )
            .subscribe((carton) => {
                this.router.navigateByUrl(
                    `/recovered-plasma/${shipmentId}/add-carton-products`
                );
            });
    }
}
