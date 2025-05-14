import { Component, inject, Inject, signal } from '@angular/core';
import { PrintableReportComponent } from '../../../../shared/components/printable-report.component';
import { DomSanitizer } from '@angular/platform-browser';
import { MAT_DIALOG_DATA, MatDialogClose, MatDialogContent, MatDialogTitle } from '@angular/material/dialog';
import { UnacceptableUnitReportOutput } from '../../graphql/query-definitions/print-unacceptable-units-report.graphql';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { BrowserPrintingService } from '../../../../core/services/browser-printing/browser-printing.service';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';

@Component({
  selector: 'biopro-view-unacceptable-products',
  standalone: true,
    imports: [
        ActionButtonComponent,
        MatIcon,
        MatDialogClose,
        MatIconButton,
        MatDialogTitle,
        MatDialogContent
    ],
  templateUrl: './view-unacceptable-products.component.html',
  styleUrl: './view-unacceptable-products.component.scss'
})
export class ViewUnacceptableProductsComponent extends PrintableReportComponent {

    browserPrintingService = inject(BrowserPrintingService);

    reportModel = signal<Partial<UnacceptableUnitReportOutput>>(null);

    constructor(
        protected domSanitizer: DomSanitizer,
        @Inject(MAT_DIALOG_DATA) protected data: UnacceptableUnitReportOutput
    ) {
        super(domSanitizer);
        this.reportModel.set(this.data);
    }

    print() {
        this.browserPrintingService.print('viewUnacceptableProductsReport');
    }

}
