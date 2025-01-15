import { Injectable } from '@angular/core';

@Injectable({
    providedIn: 'root',
})
export class ProductIconsService {
    private productIcons: { productFamily: string; value: string }[] = [
        { productFamily: 'FROZEN_PLASMA', value: 'rsa:product-plasma' },
        {
            productFamily: 'PLASMA_TRANSFUSABLE',
            value: 'rsa:product-plasma',
        },
        {
            productFamily: 'PLASMA_MFG_NONINJECTABLE',
            value: 'rsa:product-plasma',
        },
        {
            productFamily: 'PLASMA_MFG_INJECTABLE',
            value: 'rsa:product-plasma',
        },
        { productFamily: 'RED_BLOOD_CELLS', value: 'rsa:product-rbc' },
        {
            productFamily: 'RED_BLOOD_CELLS_LEUKOREDUCED',
            value: 'rsa:product-rbc',
        },
        { productFamily: 'PLATELETS', value: 'rsa:product-platelets' },
        {
            productFamily: 'WHOLE_BLOOD',
            value: 'rsa:product-rbc',
        },
        {
            productFamily: 'WHOLE_BLOOD_LEUKOREDUCED',
            value: 'rsa:product-rbc',
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
