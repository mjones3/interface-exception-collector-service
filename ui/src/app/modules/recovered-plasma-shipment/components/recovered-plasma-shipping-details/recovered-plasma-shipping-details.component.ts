import { AsyncPipe } from '@angular/common';
import { Component, computed, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { Store } from '@ngrx/store';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { Cookie } from 'app/shared/types/cookie.enum';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { CookieService } from 'ngx-cookie-service';
import { catchError, take } from 'rxjs';
import { FindShipmentRequestDTO } from '../../graphql/query-definitions/shipmentDetails.graphql';
import { RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';
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
        FuseCardComponent,
        RecoveredPlasmaShipmentDetailsNavbarComponent,
    ],
    templateUrl: './recovered-plasma-shipping-details.component.html',
})
export class RecoveredPlasmaShippingDetailsComponent implements OnInit {
    findShipmentById: RecoveredPlasmaShipmentResponseDTO;
    employeeId: string;
    protected cartonsRouteComputed = computed(() => this.router.url);

    constructor(
        public header: ProcessHeaderService,
        private router: Router,
        private toaster: ToastrImplService,
        protected route: ActivatedRoute,
        private recoveredPlasmaService: RecoveredPlasmaService,
        private cookieService: CookieService,
        private store: Store
    ) {
        this.setEmployeeId();
    }

    ngOnInit(): void {
        this.fetchRecoveredPlasmaShippingDetails();
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

    fetchRecoveredPlasmaShippingDetails() {
        return this.recoveredPlasmaService
            .getShipmentById(this.prepareShipmentIdRequest())
            .pipe(
                catchError((error: ApolloError) => {
                    handleApolloError(this.toaster, error);
                })
            )
            .subscribe({
                next: (response) => {
                    this.findShipmentById =
                        response.data?.findShipmentById?.data;
                },
            });
    }

    private prepareShipmentIdRequest(): FindShipmentRequestDTO {
        return {
            employeeId: this.employeeId,
            locationCode: this.cookieService.get(Cookie.XFacility),
            shipmentId: this.shipmentId,
        };
    }

    backToSearch() {
        this.router.navigate(['/recovered-plasma']);
    }
}
