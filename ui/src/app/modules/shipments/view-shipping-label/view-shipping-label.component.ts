import { AsyncPipe, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Observable, of } from 'rxjs';
import { PrintableReportComponent } from '../../../shared/components/printable-report.component';
import { PackingListLabelDTO } from '../models/packing-list.model';

@Component({
    selector: 'app-view-shipping-label',
    standalone: true,
    imports: [AsyncPipe, DatePipe],
    templateUrl: './view-shipping-label.component.html',
})
export class ViewShippingLabelComponent extends PrintableReportComponent {
    model$: Observable<Partial<PackingListLabelDTO>> = of();

    constructor(protected domSanitizer: DomSanitizer) {
        super(domSanitizer);
    }
}
