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
        {
            productFamily: 'APHERESIS_PLATELETS_LEUKOREDUCED',
            value: 'biopro:product-platelets',
        },
        {
            productFamily: 'PRT_APHERESIS_PLATELETS',
            value: 'biopro:product-platelets',
        },
        {
            productFamily: 'RP_FROZEN_WITHIN_120_HOURS',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'RP_FROZEN_WITHIN_24_HOURS',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'RP_NONINJECTABLE_FROZEN',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'RP_NONINJECTABLE_LIQUID_RT',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'RP_FROZEN_WITHIN_72_HOURS',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'RP_NONINJECTABLE_REFRIGERATED',
            value: 'biopro:product-plasma',
        },
        {
            productFamily: 'WASHED_APHERESIS_PLATELETS',
            value: 'biopro:product-platelets',
        },
        {
            productFamily: 'WASHED_RED_BLOOD_CELLS',
            value: 'biopro:product-rbc',
        },
        {
            productFamily: 'WASHED_PRT_APHERESIS_PLATELETS',
            value: 'biopro:product-platelets',
        },
        {
            productFamily: 'CRYOPRECIPITATE',
            value: 'biopro:cryo',
        },
        {
            productFamily: 'WASHED_APHERESIS_PLATELETS',
            value: 'biopro:product-platelets',
        },
        {
            productFamily: 'WASHED_PRT_APHERESIS_PLATELETS',
            value: 'biopro:product-platelets',
        },
        {
            productFamily: 'WASHED_RED_BLOOD_CELLS',
            value: 'biopro:product-rbc',
        }
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
