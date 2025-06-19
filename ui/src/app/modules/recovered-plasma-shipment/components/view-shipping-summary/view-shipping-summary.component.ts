import { Component, inject, Inject } from '@angular/core';
import { PrintableReportComponent } from '../../../../shared/components/printable-report.component';
import { DomSanitizer } from '@angular/platform-browser';
import { MAT_DIALOG_DATA, MatDialogClose, MatDialogTitle } from '@angular/material/dialog';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { BrowserPrintingService } from '../../../../core/services/browser-printing/browser-printing.service';
import { ShippingSummaryReportDTO } from '../../graphql/query-definitions/print-shipping-summary-report.graphql';

@Component({
  selector: 'biopro-view-shipping-summary',
  standalone: true,
    imports: [
        ActionButtonComponent,
        MatIcon,
        MatDialogClose,
        MatIconButton,
        MatDialogTitle,
    ],
  templateUrl: './view-shipping-summary.component.html',
  styleUrl: './view-shipping-summary.component.scss'
})
export class ViewShippingSummaryComponent extends PrintableReportComponent {

    browserPrintingService = inject(BrowserPrintingService);

    constructor(
        protected domSanitizer: DomSanitizer,
        @Inject(MAT_DIALOG_DATA) public data: ShippingSummaryReportDTO
    ) {
        super(domSanitizer);
    }

    print() {
        this.browserPrintingService.print('viewShippingSummaryReport');
    }

}
