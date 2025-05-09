import { Component, Inject, signal } from '@angular/core';
import { PrintableReportComponent } from '../../../../shared/components/printable-report.component';
import { DomSanitizer } from '@angular/platform-browser';
import { MAT_DIALOG_DATA } from '@angular/material/dialog';

export interface UnacceptableProductDTO {
    unitNumber: string;
    productCode: string;
    cartonNumber: string;
    cartonSequence: number;
    reasonForFailure: string;
}

export interface UnacceptableProductsReportDTO {
    shipmentNumber: number;
    products: UnacceptableProductDTO[];
}

// FIXME REMOVE MOCKS
export const mockReasons = [
    'Temperature deviation',
    'Damaged packaging',
    'Incomplete documentation',
    'Failed quality check',
    'Expired product',
    'Storage violation',
    'Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry\'s standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged.',
];
export const mockReport: UnacceptableProductsReportDTO = {
    shipmentNumber: 467622,
    products: Array.from({ length: 300 }, (_, index) => ({
        unitNumber: `W${Math.random().toString(36).substring(2, 17).toUpperCase()}`,
        productCode: `E${Math.random().toString(36).substring(2, 7).toUpperCase()}V00`,
        cartonNumber: `BRM${Math.random().toString(36).substring(2, 6).toUpperCase()}`,
        cartonSequence: 100 + (index % 5),
        reasonForFailure: mockReasons[Math.floor(Math.random() * mockReasons.length)],
    }))
};

@Component({
  selector: 'app-view-unacceptable-products',
  standalone: true,
  imports: [],
  templateUrl: './view-unacceptable-products.component.html',
  styleUrl: './view-unacceptable-products.component.scss'
})
export class ViewUnacceptableProductsComponent extends PrintableReportComponent {

    reportModel = signal<Partial<UnacceptableProductsReportDTO>>(null);

    constructor(
        protected domSanitizer: DomSanitizer,
        @Inject(MAT_DIALOG_DATA) protected data: UnacceptableProductsReportDTO
    ) {
        super(domSanitizer);
        this.reportModel.set(this.data ?? mockReport);
    }

}
