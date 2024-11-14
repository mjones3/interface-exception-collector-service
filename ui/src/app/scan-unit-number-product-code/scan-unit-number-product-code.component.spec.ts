import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
    BrowserAnimationsModule,
    NoopAnimationsModule,
} from '@angular/platform-browser/animations';
import { ScanUnitNumberProductCodeComponent } from './scan-unit-number-product-code.component';

describe('ScanUnitNumberProductCodeComponent', () => {
    let component: ScanUnitNumberProductCodeComponent;
    let fixture: ComponentFixture<ScanUnitNumberProductCodeComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ScanUnitNumberProductCodeComponent,
                BrowserAnimationsModule,
                NoopAnimationsModule,
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ScanUnitNumberProductCodeComponent);
        component = fixture.componentInstance;
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
});
