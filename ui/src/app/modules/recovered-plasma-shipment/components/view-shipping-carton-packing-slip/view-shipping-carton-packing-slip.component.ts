import { Component, Inject } from '@angular/core';
import { PrintableReportComponent } from '../../../../shared/components/printable-report.component';
import { DomSanitizer } from '@angular/platform-browser';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';
import {
    CartonPackingSlipDTO,
    PackingSlipProductDTO
} from '../../graphql/query-definitions/generate-carton-packing-slip.graphql';

interface PackingSlipProductSplitGroup {
    leftSide?: PackingSlipProductDTO;
    rightSide?: PackingSlipProductDTO;
}

@Component({
    selector: 'app-view-shipping-carton-packing-slip',
    standalone: true,
    imports: [],
    templateUrl: './view-shipping-carton-packing-slip.component.html',
    styleUrl: './view-shipping-carton-packing-slip.component.scss'
})
export class ViewShippingCartonPackingSlipComponent extends PrintableReportComponent {

    readonly splitGroupsTableRowsCount = 15;

    constructor(
        protected domSanitizer: DomSanitizer,
        @Inject(MAT_DIALOG_DATA) public data: CartonPackingSlipDTO
    ) {
        super(domSanitizer);
    }

    get reportModelProductsByGroups(): PackingSlipProductSplitGroup[] {
        return this.buildReportModelProductsSplitGroups(this.data?.products);
    }

    buildReportModelProductsSplitGroups(products: PackingSlipProductDTO[]): PackingSlipProductSplitGroup[] {
        const result: PackingSlipProductSplitGroup[] = [];
        for (let i = 0; i < products.length; i += 2) {
            const leftGroup = products[i];
            const rightGroup = products[i + 1];
            result.push({ leftSide: leftGroup, rightSide: rightGroup });
        }
        return result;
    }

}
