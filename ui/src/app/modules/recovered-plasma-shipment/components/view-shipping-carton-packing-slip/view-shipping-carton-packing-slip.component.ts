import { Component, computed, Inject, signal } from '@angular/core';
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

    reportModel = signal<Partial<CartonPackingSlipDTO>>(null);
    reportModelProductsCount = computed(() => this.reportModel()?.products?.length ?? 0);
    reportModelProductsByGroups = computed<PackingSlipProductSplitGroup[]>(() => this.buildReportModelProductsSplitGroups(this.reportModel()?.products));

    constructor(
        protected domSanitizer: DomSanitizer,
        @Inject(MAT_DIALOG_DATA) protected data: CartonPackingSlipDTO
    ) {
        super(domSanitizer);
        this.reportModel.set(this.data);
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
