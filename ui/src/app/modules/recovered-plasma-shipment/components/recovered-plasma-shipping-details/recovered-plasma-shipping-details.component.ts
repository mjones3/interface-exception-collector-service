import { AsyncPipe } from '@angular/common';
import {
    Component,
    computed,
    OnInit,
    signal,
    TemplateRef,
    viewChild,
} from '@angular/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import {
    ProcessHeaderComponent,
    ProcessHeaderService,
    TableConfiguration,
    ToastrImplService,
} from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { catchError, map, tap } from 'rxjs';
import { TableComponent } from '../../../../shared/components/table/table.component';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { RecoveredPlasmaShipmentStatus } from '../../graphql/query-definitions/shipment.graphql';
import {
    CartonDTO,
    CartonPackedItemResponseDTO,
} from '../../models/recovered-plasma.dto';
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
        MatDividerModule,
        MatIcon,
    ],
    templateUrl: './recovered-plasma-shipping-details.component.html',
})
export class RecoveredPlasmaShippingDetailsComponent
    extends RecoveredPlasmaShipmentCommon
    implements OnInit
{
    statusTemplateRef = viewChild<TemplateRef<Element>>('statusTemplateRef');
    expandTemplateRef = viewChild<TemplateRef<Element>>('expandTemplateRef');
    editTemplateRef = viewChild<TemplateRef<Element>>('editTemplateRef');

    // Signal to store expanded row data
    expandedRowDataSignal = signal<CartonPackedItemResponseDTO[]>([]);

    cartonsComputed = computed(
        () => this.shipmentDetailsSignal()?.cartonList ?? []
    );

    cartonTableConfigComputed = computed<TableConfiguration>(() => ({
        title: 'Results',
        showPagination: false,
        expandableKey: 'cartonNumber',
        columns: [
            {
                id: 'cartonNumberId',
                header: '',
                expandTemplateRef: this.expandTemplateRef(),
            },
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
                id: 'status',
                header: 'Status',
                sort: false,
                columnTempRef: this.statusTemplateRef(),
            },
            {
                id: 'actions',
                header: '',
                columnTempRef: this.editTemplateRef(),
            },
        ],
    }));

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
    }

    ngOnInit(): void {
        this.loadRecoveredPlasmaShippingDetails(
            this.routeIdComputed()
        ).subscribe();
    }

    loadCartonPackedProduct(carton: CartonDTO): void {
        if (!carton?.id) return;
        this.expandedRowDataSignal.set([]);
        this.recoveredPlasmaService
            .getCartonById(carton.id)
            .pipe(
                catchError((error: ApolloError) => {
                    handleApolloError(this.toastr, error);
                }),
                map(
                    (response) =>
                        response.data?.findCartonById?.data?.packedProducts
                )
            )
            .subscribe((data) => {
                if (data) {
                    this.expandedRowDataSignal.set(data);
                }
            });
    }

    editCarton(id: number) {
        this.router.navigate([`recovered-plasma/${id}/carton-details`]);
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
            .createCarton({ shipmentId, employeeId: this.employeeIdSignal() })
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
                return 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-blue-100 text-blue-700';
            case 'IN_PROGRESS':
                // Our current Tailwind version does not support text-orange-* and bg-orange-* shades.
                // After updating Tailwind version, replace to bg-orange-100 text-orange-700
                return 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-[#FFEDD5] text-[#C2410C]';
            case 'CLOSED':
                return 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-green-100 text-green-700';
            default:
                return '';
        }
    }
}
