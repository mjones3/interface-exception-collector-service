import { ComponentFixture, TestBed } from '@angular/core/testing';
import { createTestContext } from '@testing';
import { MarkAsUnlicensedSelectProductModal } from './select-product-modal.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';

describe('MarkAsUnlicensedSelectProductModal', () => {
    let component: MarkAsUnlicensedSelectProductModal;
    let fixture: ComponentFixture<MarkAsUnlicensedSelectProductModal>;
    let dialog: jest.Mocked<MatDialogRef<MarkAsUnlicensedSelectProductModal>>;

    beforeEach(async () => {
        dialog = { close: jest.fn() } as any;

        await TestBed.configureTestingModule({
            imports: [
                MarkAsUnlicensedSelectProductModal,
                MatIconTestingModule
            ],
            providers: [
                {
                    provide: MAT_DIALOG_DATA,
                    useValue: {}
                },
                {
                    provide: MatDialogRef,
                    useValue: dialog
                }
            ]
        }).compileComponents();

        const testContext =
            createTestContext<MarkAsUnlicensedSelectProductModal>(
                MarkAsUnlicensedSelectProductModal
            );
        fixture = testContext.fixture;
        component = testContext.component;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should close dialog when selecting a product', () => {
        const product = { productCode: 'TEST123' };
        const closeSpy = jest.spyOn(dialog, 'close');

        component.selectOption(product);
        expect(closeSpy).toHaveBeenCalledWith(product);
    });
});
