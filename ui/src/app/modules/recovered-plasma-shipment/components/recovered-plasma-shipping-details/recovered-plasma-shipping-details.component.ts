import { AsyncPipe, formatDate } from '@angular/common';
import { Component, computed, Inject, LOCALE_ID, OnInit, signal, TemplateRef, viewChild } from '@angular/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ProcessHeaderComponent, ProcessHeaderService, TableConfiguration, ToastrImplService } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { catchError, map, of, switchMap, tap } from 'rxjs';
import { TableComponent } from '../../../../shared/components/table/table.component';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { RecoveredPlasmaShipmentStatus } from '../../graphql/query-definitions/shipment.graphql';
import { CartonDTO, CartonPackedItemResponseDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaShipmentCommon } from '../../recovered-plasma-shipment.common';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import {
    ShippingInformationCardComponent
} from '../../shared/shipping-information-card/shipping-information-card.component';
import {
    RecoveredPlasmaShipmentDetailsNavbarComponent
} from '../recovered-plasma-shipment-details-navbar/recovered-plasma-shipment-details-navbar.component';
import { BrowserPrintingService } from '../../../../core/services/browser-printing/browser-printing.service';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { DEFAULT_PAGE_SIZE } from '../../../../core/models/browser-printing.model';
import {
    ViewShippingCartonPackingSlipComponent
} from '../view-shipping-carton-packing-slip/view-shipping-carton-packing-slip.component';
import { CartonPackingSlipDTO } from '../../graphql/query-definitions/generate-carton-packing-slip.graphql';
import { CloseShipmentDailogComponent } from '../close-shipment-dailog/close-shipment-dailog.component';
import { GlobalMessageComponent } from 'app/shared/components/global-message/global-message.component';
import { FuseAlertType } from '@fuse/components/alert/public-api';
import { UnsuitableUnitReportComponent } from '../../shared/unsuitable-unit-report/unsuitable-unit-report.component';

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
        GlobalMessageComponent,
        UnsuitableUnitReportComponent
    ],
    templateUrl: './recovered-plasma-shipping-details.component.html',
})
export class RecoveredPlasmaShippingDetailsComponent
    extends RecoveredPlasmaShipmentCommon
    implements OnInit
{
    messageSignal = signal<string>('Close Shipment is in progress.');
    messageTypeSignal = signal<FuseAlertType>('info');
    statusTemplateRef = viewChild<TemplateRef<Element>>('statusTemplateRef');
    expandTemplateRef = viewChild<TemplateRef<Element>>('expandTemplateRef');
    actionsTemplateRef = viewChild<TemplateRef<Element>>('actionsTemplateRef');

    // Signal to store expanded row data
    expandedRowDataSignal = signal<CartonPackedItemResponseDTO[]>([]);

    cartonsComputed = computed(
        () => this.shipmentDetailsSignal()?.cartonList ?? []
    );

    cartonTableConfigComputed = computed<TableConfiguration>(() => ({
        title: 'Carton Details',
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
                columnTempRef: this.actionsTemplateRef(),
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
        protected productIconService: ProductIconsService,
        protected browserPrintingService: BrowserPrintingService,
        protected matDialog: MatDialog,
        @Inject(LOCALE_ID) public locale: string
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
        this.fetchShipmentData(this.routeIdComputed())
    }

    fetchShipmentData(id: number){
        this.loadRecoveredPlasmaShippingDetails(id)
            .pipe(
                tap(() => {
                    if (this.shouldPrintCartonPackingSlip) {
                        this.printCarton(null, this.shipmentCloseCartonId);
                    }
                })
            )
            .subscribe();
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

    printCarton(event: Event, cartonId: number) {
        // This is to prevent event bubbling, avoiding triggering the table row expansion
        event?.stopPropagation();

        let dialogRef: MatDialogRef<ViewShippingCartonPackingSlipComponent, CartonPackingSlipDTO>;
        this.recoveredPlasmaService
            .generateCartonPackingSlip({
                cartonId: cartonId,
                employeeId: this.employeeIdSignal(),
                locationCode: this.locationCodeComputed()
            })
            .pipe(
                catchError((error: ApolloError) => {
                    handleApolloError(this.toastr, error);
                }),
                switchMap(response => {
                    if (response?.data?.generateCartonPackingSlip?.notifications?.[0]?.type === 'SUCCESS') {
                        dialogRef = this.matDialog
                            .open(ViewShippingCartonPackingSlipComponent, {
                                id: 'ViewShippingCartonPackingSlipDialog',
                                hasBackdrop: false,
                                panelClass: 'hidden',
                                data: response.data.generateCartonPackingSlip?.data
                            });
                        return dialogRef.afterOpened();
                    }
                    consumeUseCaseNotifications(
                        this.toastr,
                        response.data.generateCartonPackingSlip?.notifications
                    )
                    return of({});
                }),
                tap(() => {
                    this.browserPrintingService
                        .print('viewShippingCartonPackingSlipReport', { pageSize: DEFAULT_PAGE_SIZE });
                    dialogRef?.close();
                }),
            )
            .subscribe();
    }

    get cartonsRoute(): string {
        return this.router.url;
    }

    get shipmentId(): number {
        return parseInt(this.route.snapshot.params?.id);
    }

    get shipmentCloseCartonId(): number {
        return parseInt(this.route.snapshot.queryParams?.closeCartonId);
    }

    get shouldPrintCartonPackingSlip(): boolean {
        const shouldPrint = this.route.snapshot.queryParams?.print === 'true';
        const hasValidCartonId = !isNaN(parseInt(this.route.snapshot.queryParams?.closeCartonId))
        return shouldPrint && hasValidCartonId;
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

    // implement styles for shipment status
    getStatusInformationCard(status: keyof typeof RecoveredPlasmaShipmentStatus) {
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

    verifyProducts(id: number){
        this.router.navigate([`recovered-plasma/${id}/verify-carton`])
    }

    handleCompleteShipmentContinue(result){
        if (result) {
            const formatShipDate = formatDate(
                result,
                'yyyy-MM-dd',
                this.locale
            );
            this.recoveredPlasmaService
                .closeShipment({
                    locationCode: this.locationCodeComputed(),
                    shipmentId: this.shipmentId,
                    shipDate: formatShipDate,
                    employeeId: this.employeeIdSignal(),
                })
                .pipe(
                    catchError((error: ApolloError) => {
                        handleApolloError(this.toastr, error);
                    }),
                    tap((response) =>
                        consumeUseCaseNotifications(
                            this.toastr,
                            response.data?.closeShipment?.notifications
                        )
                    )
                )
                .subscribe((response) => {
                    if (response?.data?.closeShipment?.data) {
                        this.fetchShipmentData(response.data.closeShipment.data.id)
                    }
                });
        }
    }

    onClickCloseShipment() {
        this.matDialog.open(CloseShipmentDailogComponent, {
            width: '24rem',
            disableClose: true,
            data: {
                shipmentDate: this.shipmentDetailsSignal().shipmentDate,
                continueFn: this.handleCompleteShipmentContinue.bind(this)
            }
        })
    }
}
