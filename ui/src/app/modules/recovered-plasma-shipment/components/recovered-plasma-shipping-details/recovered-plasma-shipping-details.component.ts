import { AsyncPipe } from '@angular/common';
import {
    Component,
    computed,
    OnInit,
    TemplateRef,
    viewChild,
} from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    TableConfiguration,
    ToastrImplService,
} from '@shared';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { catchError, take, tap } from 'rxjs';
import { TableComponent } from '../../../../shared/components/table/table.component';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { RecoveredPlasmaShipmentStatus } from '../../graphql/query-definitions/shipment.graphql';
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
        TableComponent,
    ],
    templateUrl: './recovered-plasma-shipping-details.component.html',
})
export class RecoveredPlasmaShippingDetailsComponent
    extends RecoveredPlasmaShipmentCommon
    implements OnInit
{
    findShipmentById: RecoveredPlasmaShipmentResponseDTO;
    employeeId: string;
    cartonsComputed = computed(
        () => this.shipmentDetailsSignal()?.cartonList ?? []
    );
    statusTemplateRef = viewChild<TemplateRef<Element>>('statusTemplateRef');

    cartonTableConfig: TableConfiguration = {
        showPagination: false,
        columns: [
            {
                id: 'cartonNumber',
                header: 'Carton Number',
                sort: false,
            },
            {
                id: 'cartonSequence',
                header: 'Sequence',
                sort: false,
            },
            {
                id: 'totalProducts',
                header: 'Total Products',
                sort: false,
            },
            {
                id: 'status',
                header: 'Status',
                sort: false,
                columnTempRef: this.statusTemplateRef(),
            },
        ],
    };

    constructor(
        public header: ProcessHeaderService,
        protected store: Store,
        protected route: ActivatedRoute,
        protected router: Router,
        protected toastr: ToastrImplService,
        protected recoveredPlasmaService: RecoveredPlasmaService,
        protected cookieService: CookieService,
        protected productIconService: ProductIconsService
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

    get cartonsRoute(): string {
        return this.router.url;
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
                    handleApolloError(this.toastr, error);
                }),
                tap((response) =>
                    consumeUseCaseNotifications(
                        this.toastr,
                        response.data?.createCarton?.notifications
                    )
                )
            )
            .subscribe((carton) => {
                const nextUrl = carton?.data?.createCarton?._links.next;
                if (nextUrl) {
                    this.router.navigateByUrl(nextUrl);
                }
            });
    }

    getStatusBadgeCssClass(status: keyof typeof RecoveredPlasmaShipmentStatus) {
        switch (status) {
            case 'OPEN':
                return 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-blue-200 text-blue-700';
            default:
                return '';
        }
    }
}
