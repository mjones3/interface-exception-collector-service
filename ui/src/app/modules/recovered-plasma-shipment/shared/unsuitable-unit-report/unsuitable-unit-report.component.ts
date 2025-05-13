import { Component, computed, inject, input } from '@angular/core';
import { Description, DescriptionCardComponent, ToastrImplService, WidgetComponent } from '@shared';
import { LoadingSpinnerComponent } from 'app/shared/components/loading-spinner/loading-spinner.component';
import { RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';
import { DatePipe } from '@angular/common';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import {
    ViewUnacceptableProductsComponent
} from '../../components/view-unacceptable-products/view-unacceptable-products.component';
import { UnacceptableUnitReportOutput } from '../../graphql/query-definitions/print-unacceptable-units-report.graphql';
import { catchError, of, tap } from 'rxjs';
import { ApolloError } from '@apollo/client';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import {
    DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
    DEFAULT_PAGE_SIZE_DIALOG_PORTRAIT_WIDTH
} from '../../../../core/models/browser-printing.model';

@Component({
    selector: 'biopro-unsuitable-unit-report',
    standalone: true,
    imports: [
        WidgetComponent,
        LoadingSpinnerComponent,
        DescriptionCardComponent,
        ActionButtonComponent,
    ],
    templateUrl: './unsuitable-unit-report.component.html'
})
export class UnsuitableUnitReportComponent {

    readonly loaderMessage = 'Unacceptable Products Report is in progress';

    datePipe = inject(DatePipe);
    recoveredPlasmaService = inject(RecoveredPlasmaService);
    toastr = inject(ToastrImplService);
    matDialog = inject(MatDialog);

    loading = input<boolean>(false);
    shipment = input.required<RecoveredPlasmaShipmentResponseDTO>();
    employeeId = input.required<string>();
    locationCode = input.required<string>();
    unsuitableReportInfo = computed<Description[]>(() => [
        ...(this.shipment()?.lastUnsuitableReportRunDate
            ? [
                {
                    label: 'Last Run',
                    value: this.datePipe.transform(this.shipment()?.lastUnsuitableReportRunDate, 'MM/dd/yyyy HH:mm')
                },
            ]
            : []),
    ]);

    viewReport() {
        let dialogRef: MatDialogRef<ViewUnacceptableProductsComponent, UnacceptableUnitReportOutput>;
        this.recoveredPlasmaService
            .printUnacceptableUnitsReport({
                shipmentId: this.shipment().id,
                employeeId: this.employeeId(),
                locationCode: this.locationCode()
            })
            .pipe(
                catchError((error: ApolloError) => handleApolloError(this.toastr, error)),
                tap(response => {
                    if (response?.data?.printUnacceptableUnitsReport?.notifications?.[0]?.type === 'SUCCESS') {
                        dialogRef = this.matDialog
                            .open(ViewUnacceptableProductsComponent, {
                                id: 'viewUnacceptableProductsDialog',
                                width: DEFAULT_PAGE_SIZE_DIALOG_PORTRAIT_WIDTH,
                                height: DEFAULT_PAGE_SIZE_DIALOG_HEIGHT,
                                data: response.data?.printUnacceptableUnitsReport?.data
                            });
                        return dialogRef.afterOpened();
                    }
                    consumeUseCaseNotifications(this.toastr, response.data?.printUnacceptableUnitsReport?.notifications);
                    return of({});
                }),
            )
            .subscribe();
    }

}
