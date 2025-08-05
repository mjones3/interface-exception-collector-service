import { AsyncPipe, DatePipe } from '@angular/common';
import { Component } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { Observable, of } from 'rxjs';
import { PrintableReportComponent } from '../../../shared/components/printable-report.component';
import { ProductFamilyMap } from '../../../shared/models/product-family.model';
import { PackingListLabelDTO } from '../models/packing-list.model';

@Component({
    selector: 'app-view-packing-list',
    standalone: true,
    imports: [AsyncPipe, DatePipe],
    templateUrl: './view-packing-list.component.html',
})
export class ViewPackingListComponent extends PrintableReportComponent {
    protected readonly ProductFamilyMap = ProductFamilyMap;

    model$: Observable<Partial<PackingListLabelDTO>> = of();

    constructor(protected domSanitizer: DomSanitizer) {
        super(domSanitizer);
    }

    getPackedItemsQuantity(packingListLabelDTO: Partial<PackingListLabelDTO>) {
        return packingListLabelDTO?.packedItems?.length ?? 0;
    }
}
