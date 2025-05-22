import { AsyncPipe, formatDate } from '@angular/common';
import {
    Component,
    computed,
    Inject,
    LOCALE_ID,
    OnDestroy,
    OnInit,
    signal,
    TemplateRef,
    viewChild
} from '@angular/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatIcon } from '@angular/material/icon';
import { ActivatedRoute, Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ProcessHeaderComponent, ProcessHeaderService, TableConfiguration } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { CookieService } from 'ngx-cookie-service';
import { catchError, filter, map, of, Subscription, switchMap, take, takeWhile, tap, timer } from 'rxjs';
import { TableComponent } from '../../../../shared/components/table/table.component';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import {
    RecoveredPlasmaCartonStatusCssMap,
    RecoveredPlasmaCartonStatusMap
} from '../../graphql/query-definitions/shipment.graphql';
import {
    CartonDTO,
    CartonPackedItemResponseDTO,
    RecoveredPlasmaShipmentResponseDTO
} from '../../models/recovered-plasma.dto';
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
import {
    DEFAULT_PAGE_SIZE,
    DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
    DEFAULT_PAGE_SIZE_DIALOG_PORTRAIT_WIDTH
} from '../../../../core/models/browser-printing.model';
import {
    ViewShippingCartonPackingSlipComponent
} from '../view-shipping-carton-packing-slip/view-shipping-carton-packing-slip.component';
import { CartonPackingSlipDTO } from '../../graphql/query-definitions/generate-carton-packing-slip.graphql';
import { ViewShippingSummaryComponent } from '../view-shipping-summary/view-shipping-summary.component';
import { CloseShipmentDailogComponent } from '../close-shipment-dailog/close-shipment-dailog.component';
import { GlobalMessageComponent } from 'app/shared/components/global-message/global-message.component';
import { FuseAlertType } from '@fuse/components/alert/public-api';
import {
    UnacceptableProductsReportWidgetComponent
} from '../../shared/unacceptable-products-report-widget/unacceptable-products-report-widget.component';
import { RepackCartonDialogComponent } from '../repack-carton-dialog/repack-carton-dialog.component';
import { ToastrService } from 'ngx-toastr';
import { ShippingSummaryReportDTO } from '../../graphql/query-definitions/print-shipping-summary-report.graphql';
import { FuseConfirmationService } from '@fuse/services/confirmation';

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
        UnacceptableProductsReportWidgetComponent
    ],
    templateUrl: './recovered-plasma-shipping-details.component.html',
})
export class RecoveredPlasmaShippingDetailsComponent
    extends RecoveredPlasmaShipmentCommon
    implements OnInit, OnDestroy
{
    protected readonly RecoveredPlasmaCartonStatusMap = RecoveredPlasmaCartonStatusMap;
    protected readonly RecoveredPlasmaCartonStatusCssMap = RecoveredPlasmaCartonStatusCssMap;

    protected static readonly POLLING_INTERVAL = 10 * 1000; // 10 seconds
    protected static readonly POLLING_MAX_TIMEOUT = 120 * 1000; // 120 seconds

    messageSignal = signal<string>(null);
    messageTypeSignal = signal<FuseAlertType>(null);
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

    pollingSubscription: Subscription;
    constructor(
        public header: ProcessHeaderService,
        protected store: Store,
        protected route: ActivatedRoute,
        protected router: Router,
        protected toastr: ToastrService,
        protected recoveredPlasmaService: RecoveredPlasmaService,
        protected cookieService: CookieService,
        protected productIconService: ProductIconsService,
        protected browserPrintingService: BrowserPrintingService,
        protected matDialog: MatDialog,
        private fuseConfirmationService: FuseConfirmationService,
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
        this.fetchShipmentData(this.routeIdComputed());
    }

    ngOnDestroy() {
        if (this.pollingSubscription && !this.pollingSubscription.closed) {
            this.pollingSubscription.unsubscribe();
        }
    }

    fetchShipmentData(id: number) {
        this.pollingSubscription = timer(0, RecoveredPlasmaShippingDetailsComponent.POLLING_INTERVAL)
            .pipe(
                take(RecoveredPlasmaShippingDetailsComponent.POLLING_MAX_TIMEOUT / RecoveredPlasmaShippingDetailsComponent.POLLING_INTERVAL),
                switchMap(() => this.loadRecoveredPlasmaShippingDetails(id)),
                tap((shipping: RecoveredPlasmaShipmentResponseDTO) => {
                    if (this.shouldPrintCartonPackingSlip) {
                        this.printCarton(null, this.shipmentCloseCartonId);
                    }
                    this.configureUnacceptableProductsReportStatusMessage();
                }),
                takeWhile((shipping: RecoveredPlasmaShipmentResponseDTO) => shipping.status === 'PROCESSING', true)
            )
            .subscribe();
    }

    configureUnacceptableProductsReportStatusMessage(){
        // Processing unacceptable products report
        if (this.shipmentDetailsSignal()?.status === 'PROCESSING') {
            this.messageSignal.set('Close Shipment is in progress.');
            this.messageTypeSignal.set('info');
            return;
        }

        // No more processing, check if has any processing error
        if (this.shipmentDetailsSignal()?.unsuitableUnitReportDocumentStatus === 'ERROR_PROCESSING') {
            this.messageSignal.set('Unacceptable Products report generation error. Contact Support.');
            this.messageTypeSignal.set('info');
            return;
        }

        // Nothing to process and no error
        this.messageSignal.set(null);
        this.messageTypeSignal.set(null);
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
                    this.router.navigate([`recovered-plasma/${this.shipmentId}/shipment-details`]);
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

    openReportsDialog(): void {
        let dialogRef: MatDialogRef<ViewShippingSummaryComponent, ShippingSummaryReportDTO>;
        this.recoveredPlasmaService
            .printShippingSummaryReport({
                shipmentId: this.shipmentIdComputed(),
                employeeId: this.employeeIdSignal(),
                locationCode: this.locationCodeComputed()
            })
            .pipe(
                catchError((error: ApolloError) => handleApolloError(this.toastr, error)),
                tap(response => {
                    if (response?.data?.printShippingSummaryReport?.notifications?.[0]?.type === 'SUCCESS') {
                        dialogRef = this.matDialog
                            .open(ViewShippingSummaryComponent, {
                                id: 'ViewShippingSummaryDialog',
                                width: DEFAULT_PAGE_SIZE_DIALOG_PORTRAIT_WIDTH,
                                height: DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
                                data: response.data?.printShippingSummaryReport?.data,
                            });
                        return dialogRef.afterOpened();
                    }
                    consumeUseCaseNotifications(this.toastr, response.data?.printShippingSummaryReport?.notifications);
                    return of({});
                }),
            )
            .subscribe();
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

    handleCloseShipmentContinue(result){
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
                continueFn: this.handleCloseShipmentContinue.bind(this)
            }
        })
    }


    // Opens dialog confirmation for repack
    repackCarton(cartonId: number){
        this.matDialog.open(RepackCartonDialogComponent, {
            width: '24rem',
            disableClose: true
        })
        .afterClosed()
        .subscribe((req) => {
           if(req !== undefined){
                this.recoveredPlasmaService
                .repackCarton({
                    locationCode: this.locationCodeComputed(),
                    cartonId: cartonId,
                    comments: req,
                    employeeId: this.employeeIdSignal(),
                })
                .pipe(
                    catchError((error: ApolloError) => {
                        handleApolloError(this.toastr, error);
                    }),
                    tap((response) =>
                        consumeUseCaseNotifications(
                            this.toastr,
                            response.data.repackCarton.notifications
                        )
                    )
                )
                .subscribe((response) => {
                    if (response?.data?.repackCarton) {
                        const nextUrl = response.data.repackCarton._links?.next;
                        if (nextUrl) {
                            this.router.navigateByUrl(nextUrl);
                        }
                    }
                });
            }
        })
    }

    // Opens remove carton confirmation dialog
    removeCarton(id: number){
        const dialogRef = this.fuseConfirmationService.open({
            title: 'Remove Confirmation',
            message: 'Carton will be removed. <b>Are you sure you want to continue?</b>',
            dismissible: false,
            icon: {
                show: false,
            },
            actions: {
                confirm: {
                    label: 'Continue',
                    class: 'bg-red-700 text-white',
                },
                cancel: {
                    class: 'mat-secondary',
                },
            },
        })
        dialogRef.afterClosed()
        .pipe(filter((value) => 'confirmed' === value)) 
        .subscribe(() => {
            console.log('remove carton');

                this.recoveredPlasmaService
                .removeLastCarton({
                    cartonId: id,
                    employeeId: this.employeeIdSignal(),
                })
                .pipe(
                    catchError((error: ApolloError) => {
                        handleApolloError(this.toastr, error);
                    }),
                    tap((response) =>
                        consumeUseCaseNotifications(
                            this.toastr,
                            response.data.removeCarton.notifications
                        )
                    )
                )
                .subscribe((response) => {
                    const res = response?.data?.removeCarton.notifications[0].type === 'SUCCESS';
                    if(res){
                        this.shipmentDetailsSignal.set(response.data.removeCarton.data);
                    }
                });
        })

    }
}
