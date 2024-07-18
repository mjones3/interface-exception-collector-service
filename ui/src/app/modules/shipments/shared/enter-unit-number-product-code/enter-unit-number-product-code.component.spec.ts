import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import {
    TranslateFakeLoader,
    TranslateLoader,
    TranslateModule,
} from '@ngx-translate/core';
import { ScanUnitNumberCheckDigitComponent } from '../../../../shared/components/scan-unit-number-check-digit/scan-unit-number-check-digit.component';
import { EnterUnitNumberProductCodeComponent } from './enter-unit-number-product-code.component';

describe('EnterUnitNumberProductCodeComponent', () => {
    let component: EnterUnitNumberProductCodeComponent
    let fixture: ComponentFixture<EnterUnitNumberProductCodeComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                EnterUnitNumberProductCodeComponent,
                ScanUnitNumberCheckDigitComponent,
                BrowserAnimationsModule,
                TranslateModule.forRoot({
                    loader: {
                        provide: TranslateLoader,
                        useClass: TranslateFakeLoader,
                    },
                }),
            ],
            providers: [provideHttpClientTesting()],
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EnterUnitNumberProductCodeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
