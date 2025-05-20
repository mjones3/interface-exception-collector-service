import { Component, inject, Inject } from '@angular/core';
import { PrintableReportComponent } from '../../../../shared/components/printable-report.component';
import { DomSanitizer } from '@angular/platform-browser';
import { MAT_DIALOG_DATA, MatDialogClose, MatDialogTitle } from '@angular/material/dialog';
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
        MatDialogTitle
    ],
  templateUrl: './view-unacceptable-products.component.html',
  styleUrl: './view-unacceptable-products.component.scss'
})
export class ViewUnacceptableProductsComponent extends PrintableReportComponent {

    browserPrintingService = inject(BrowserPrintingService);

    constructor(
        protected domSanitizer: DomSanitizer,
        @Inject(MAT_DIALOG_DATA) public data: UnacceptableUnitReportOutput
    ) {
        super(domSanitizer);
    }

    print() {
        this.browserPrintingService.print('viewUnacceptableProductsReport');
    }

}
