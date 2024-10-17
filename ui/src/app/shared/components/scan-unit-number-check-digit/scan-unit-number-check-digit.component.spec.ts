import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateModule } from '@ngx-translate/core';
import { ScanUnitNumberCheckDigitComponent } from './scan-unit-number-check-digit.component';

describe('ScanUnitNumberCheckDigitComponent', () => {
    let component: ScanUnitNumberCheckDigitComponent;
    let fixture: ComponentFixture<ScanUnitNumberCheckDigitComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ScanUnitNumberCheckDigitComponent,
                NoopAnimationsModule,
                TranslateModule.forRoot(),
            ],
            providers: [],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(ScanUnitNumberCheckDigitComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should hide check digit field when showCheckDigit is false', () => {
        component.showCheckDigit = false;
        fixture.detectChanges();
        expect(
            fixture.debugElement.nativeElement.querySelector('#inCheckDigit')
        ).toBeFalsy();
    });
});
