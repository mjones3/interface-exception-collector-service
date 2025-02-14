import { TestBed } from '@angular/core/testing';
import { ProductIconsService } from './product-icon.service';

describe('ProductIconsService', () => {
    let service: ProductIconsService;

    beforeEach(() => {
        TestBed.configureTestingModule({});
        service = TestBed.inject(ProductIconsService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should return productIcons based on Product Family', () => {
        const productIconMock: { productFamily: string; value: string }[] = [
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
        expect(service.getProductIcons()).toEqual(productIconMock);
        expect(service.getIconByProductFamily('WHOLE_BLOOD')).toBe(
            'biopro:product-whole-blood'
        );
        expect(service.getIconByProductFamily('BLOOD')).toBeUndefined();
    });
});
