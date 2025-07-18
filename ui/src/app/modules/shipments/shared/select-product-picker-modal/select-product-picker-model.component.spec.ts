import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { SelectProductPickerModalComponent } from './select-product-picker-modal.component';
import { ProductIconsService } from '../../../../shared/services/product-icon.service';
import { ProductResponseDTO } from '../../graphql/query-defintions/get-unlabeled-products.graphql';
import { OptionPicker } from '@shared';

describe('SelectProductPickerModalComponent', () => {
    let component: SelectProductPickerModalComponent;
    let fixture: ComponentFixture<SelectProductPickerModalComponent>;
    let mockDialogRef: jest.Mocked<MatDialogRef<SelectProductPickerModalComponent>>;
    let mockProductIconsService: jest.Mocked<ProductIconsService>;

    const mockProductData: ProductResponseDTO[] = [
        {
            inventoryId: '1',
            unitNumber: 'UNIT001',
            productCode: 'PC001',
            aboRh: 'OP',
            productDescription: 'Test Product 1',
            productFamily: 'FROZEN_PLASMA',
            status: 'ACTIVE',
            isLabeled: false,
            isLicensed: true
        },
        {
            inventoryId: '2',
            unitNumber: 'UNIT002',
            productCode: 'PC002',
            aboRh: 'AP',
            productDescription: 'Test Product 2',
            productFamily: 'RED_BLOOD_CELLS',
            status: 'ACTIVE',
            isLabeled: false,
            isLicensed: true
        }
    ];

    beforeEach(async () => {
        mockDialogRef = {
            close: jest.fn()
        } as any;

        mockProductIconsService = {
            getIconByProductFamily: jest.fn()
        } as any;

        await TestBed.configureTestingModule({
            imports: [SelectProductPickerModalComponent],
            providers: [
                { provide: MatDialogRef, useValue: mockDialogRef },
                { provide: MAT_DIALOG_DATA, useValue: mockProductData },
                { provide: ProductIconsService, useValue: mockProductIconsService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(SelectProductPickerModalComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize signals with default values', () => {
        expect(component.unitNumber()).toBeNull();
        expect(component.products()).toEqual([]);
    });

    it('should call productIconsService for getItemIcon', () => {
        mockProductIconsService.getIconByProductFamily.mockReturnValue('test-icon');

        const result = component.getItemIcon('FROZEN_PLASMA');

        expect(mockProductIconsService.getIconByProductFamily).toHaveBeenCalledWith('FROZEN_PLASMA');
        expect(result).toBe('test-icon');
    });

    it('should close dialog with option', () => {
        const option: OptionPicker = { icon: 'test-icon', statuses: [] };

        component.selectOptionChange(option);

        expect(mockDialogRef.close).toHaveBeenCalledWith(option);
    });

    it('should close dialog with array of options', () => {
        const options: OptionPicker[] = [{ icon: 'test-icon', statuses: [] }];

        component.selectOptionChange(options);

        expect(mockDialogRef.close).toHaveBeenCalledWith(options);
    });
});
