import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import {
    BrowserAnimationsModule,
    NoopAnimationsModule,
} from '@angular/platform-browser/animations';
import { UppercaseDirective } from 'app/shared/directive/uppercase/uppercase.directive';
import { ScanUnitNumberProductCodeComponent } from './scan-unit-number-product-code.component';
describe('ScanUnitNumberProductCodeComponent', () => {
    let component: ScanUnitNumberProductCodeComponent;
    let fixture: ComponentFixture<ScanUnitNumberProductCodeComponent>;
    let inputValue: HTMLInputElement;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ScanUnitNumberProductCodeComponent,
                BrowserAnimationsModule,
                NoopAnimationsModule,
                UppercaseDirective,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ScanUnitNumberProductCodeComponent);
        component = fixture.componentInstance;
        inputValue = fixture.debugElement.query(By.css('input')).nativeElement;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should show message if Unit Number is entered manually', () => {
        const unitNumber = component.unitProductGroup.get('unitNumber');
        unitNumber?.setValue('W121212');
        fixture.detectChanges();
        expect(unitNumber.hasError('manualEntryUnitNumber')).toBeTruthy();
    });

    it('should  allow to scan Unit Number', () => {
        const unitNumber = component.unitProductGroup.get('unitNumber');
        unitNumber?.setValue('=W1212121212300');
        fixture.detectChanges();
        expect(unitNumber.hasError('manualEntryUnitNumber')).toBeFalsy();
    });

    it('should show message if Product Code is entered manually', () => {
        component.unitProductGroup.controls.unitNumber.valid;
        component.unitProductGroup.controls.productCode.enable();
        const productCode = component.unitProductGroup.get('productCode');
        productCode?.setValue('122');
        fixture.detectChanges();
        expect(productCode.hasError('manualEntryProductCode')).toBeTruthy();
    });

    it('should  allow to scan Product Code', () => {
        component.unitProductGroup.controls.unitNumber.valid;
        component.unitProductGroup.controls.productCode.enable();
        const productCode = component.unitProductGroup.get('productCode');
        productCode?.setValue('=<E1624V00');
        fixture.detectChanges();
        expect(productCode.hasError('manualEntryProductCode')).toBeFalsy();
    });

    it('Should disable Product Code field  when Unit Number is manually entered', () => {
        const unitNumber = component.unitProductGroup.get('unitNumber');
        unitNumber?.setValue('wewew');
        fixture.detectChanges();
        component.enableProductCode();
        expect(
            component.unitProductGroup.controls.productCode.disable
        ).toBeTruthy();
    });

    it('Should enable Product Code field  when Unit Number is scanned', () => {
        const unitNumber = component.unitProductGroup.get('unitNumber');
        unitNumber?.setValue('=W1212121212300');
        fixture.detectChanges();
        component.enableProductCode();
        expect(
            component.unitProductGroup.controls.productCode.enable
        ).toBeTruthy();
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

    it('Should convert input text to Uppercase', () => {
        inputValue.value = 'abc123';
        inputValue.dispatchEvent(new Event('input'));
        fixture.detectChanges();
        expect(inputValue.value).toBe('ABC123');
    });
});
