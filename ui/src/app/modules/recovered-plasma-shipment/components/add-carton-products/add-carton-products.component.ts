import { AsyncPipe } from '@angular/common';
import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    ToastrImplService,
} from '@shared';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { ShippingInformationCardComponent } from '../../shared/shipping-information-card/shipping-information-card.component';
import { CartonDTO } from '../../models/recovered-plasma.dto';
import { ShippingCartonInformationCardComponent } from '../../shared/shipping-carton-information-card/shipping-carton-information-card.component';
import { catchError, map, Observable, switchMap, tap } from 'rxjs';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { Store } from '@ngrx/store';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { ApolloError } from '@apollo/client';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';

@Component({
    selector: 'app-add-carton-products',
    standalone: true,
    imports: [
        AsyncPipe,
        ProcessHeaderComponent,
        ActionButtonComponent,
        ShippingInformationCardComponent,
        ShippingCartonInformationCardComponent,
    ],
    templateUrl: './add-carton-products.component.html',
})
export class AddCartonProductsComponent
    extends RecoveredPlasmaShipmentCommon
    implements OnInit
{
    protected cartonDetailsSignal = signal<CartonDTO>(null);

    constructor(
        public header: ProcessHeaderService,
        protected route: ActivatedRoute,
        protected router: Router,
        protected store: Store,
        protected recoveredPlasmaService: RecoveredPlasmaService,
        protected toastr: ToastrImplService,
        protected productIconService: ProductIconsService,
        protected cookieService: CookieService
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

    ngOnInit(): void {
        this.loadRecoveredPlasmaShippingCartonDetails(this.routeIdComputed())
            .pipe(
                switchMap((carton) =>
                    this.loadRecoveredPlasmaShippingDetails(carton.shipmentId)
                )
            )
            .subscribe();
    }

    loadRecoveredPlasmaShippingCartonDetails(
        id: number
    ): Observable<CartonDTO> {
        return this.recoveredPlasmaService.getCartonById(id).pipe(
            catchError((error: ApolloError) => {
                handleApolloError(this.toastr, error);
            }),
            tap((response) =>
                consumeUseCaseNotifications(
                    this.toastr,
                    response.data?.findCartonById?.notifications
                )
            ),
            map((response) => {
                const { data } = response.data.findCartonById;
                this.cartonDetailsSignal.set(data);
                return data;
            })
        );
    }

    backToShipment() {
        this.router.navigateByUrl(
            `/recovered-plasma/${this.shipmentIdComputed()}/shipment-details`
        );
    }
}
