import { ComponentFixture, TestBed } from '@angular/core/testing';
import { createTestContext } from '@testing';
import { IrradiationSelectProductModal } from './select-product-modal.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatIconTestingModule } from '@angular/material/icon/testing';

describe('MarkAsUnlicensedSelectProductModal', () => {
    let component: IrradiationSelectProductModal;
    let fixture: ComponentFixture<IrradiationSelectProductModal>;
    let dialog: jest.Mocked<MatDialogRef<IrradiationSelectProductModal>>;

    beforeEach(async () => {
        dialog = { close: jest.fn() } as any;

        await TestBed.configureTestingModule({
            imports: [
                IrradiationSelectProductModal,
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
            createTestContext<IrradiationSelectProductModal>(
                IrradiationSelectProductModal
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
