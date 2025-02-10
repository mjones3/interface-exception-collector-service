import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class ProductIconsService {
    private productIcons: { productFamily: string; value: string }[] = [
        { productFamily: 'FROZEN_PLASMA', value: 'biopro:product-plasma' },
        {
            productFamily: 'PLASMA_TRANSFUSABLE',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'PLASMA_MFG_NONINJECTABLE',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'PLASMA_MFG_INJECTABLE',
            value: 'biopro:product-plasma',
        },
        { productFamily: 'RED_BLOOD_CELLS', value: 'biopro:product-rbc' },
        {
            productFamily: 'RED_BLOOD_CELLS_LEUKOREDUCED',
            value: 'biopro:product-rbc',
        },
        { productFamily: 'PLATELETS', value: 'biopro:product-platelets' },
        {
            productFamily: 'WHOLE_BLOOD',
            value: 'biopro:product-whole-blood',
        },
        {
            productFamily: 'WHOLE_BLOOD_LEUKOREDUCED',
            value: 'biopro:product-whole-blood',
        },
    ];

    getProductIcons() {
        return this.productIcons;
    }

    getIconByProductFamily(productFamily: string) {
        return this.productIcons.find(
            (productIcon) => productIcon.productFamily === productFamily
        )?.value;
    }
}
