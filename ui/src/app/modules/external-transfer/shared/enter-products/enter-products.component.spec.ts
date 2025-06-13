import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { By } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { UppercaseDirective } from 'app/shared/directive/uppercase/uppercase.directive';
import { EnterProductsComponent } from './enter-products.component';

describe('EnterProductsComponent', () => {
    let component: EnterProductsComponent;
    let fixture: ComponentFixture<EnterProductsComponent>;
    let inputValue: HTMLInputElement;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                EnterProductsComponent,
                BrowserAnimationsModule,
                UppercaseDirective,
                ReactiveFormsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(EnterProductsComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        inputValue = fixture.debugElement.query(By.css('input')).nativeElement;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('Should convert input text to Uppercase', () => {
        inputValue.value = 'abc123';
        inputValue.dispatchEvent(new Event('input'));
        fixture.detectChanges();
        expect(inputValue.value).toBe('ABC123');
    });

    it('should emit validate', () => {
        const emitSpy = jest.spyOn(component.validate, 'emit');
        fixture.detectChanges();
        const products = {
            unitNumber: 'W036898786801',
            productCode: 'E7646V00',
        };
        component.productGroup.patchValue(products);
        component.verifyProduct();
        expect(emitSpy).toHaveBeenCalledWith(products);
    });

    it('should allow to enter Unit Number manually', () => {
        const unitNumber = component.productGroup.get('unitNumber');
        unitNumber?.setValue('W12121212123');
        fixture.detectChanges();
        expect(component.productGroup.controls.unitNumber.value).toBe(
            'W12121212123'
        );
    });

    it('should not allow to scan Unit Number', () => {
        const unitNumber = component.productGroup.get('unitNumber');
        unitNumber?.setValue('=W1212121212300');
        fixture.detectChanges();
        expect(unitNumber.hasError('pattern')).toBeTruthy();
    });

    it('should allow to enter Product Code manually', () => {
        const productCode = component.productGroup.get('productCode');
        productCode?.setValue('E0869V00');
        fixture.detectChanges();
        expect(component.productGroup.controls.productCode.value).toBe(
            'E0869V00'
        );
    });

    it('should not allow to scan Product Code', () => {
        const unitNumber = component.productGroup.get('productCode');
        unitNumber?.setValue('=<E0869V00');
        fixture.detectChanges();
        expect(unitNumber.hasError('pattern')).toBeTruthy();
    });

    it('should hide required asterisk', () => {
        const matFormFieldTextLabel = fixture.debugElement.query(
            By.css('mat-form-field')
        );
        expect(matFormFieldTextLabel.componentInstance.hideRequiredMarker).toBe(
            true
        );
        const isAsterisk = fixture.debugElement.nativeElement.querySelector(
            '.mat-mdc-form-field-required-marker'
        );
        expect(isAsterisk).toBeNull();
    });
});
