import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { getAppInitializerMockProvider, RsaCommonsModule } from '@rsa/commons';
import { TouchableComponentsModule } from '@rsa/touchable';
import { ScanUnitNumberCheckDigitComponent } from './scan-unit-number-check-digit.component';

describe('ScanUnitNumberCheckDigitComponent', () => {
  let component: ScanUnitNumberCheckDigitComponent;
  let fixture: ComponentFixture<ScanUnitNumberCheckDigitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ScanUnitNumberCheckDigitComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        RsaCommonsModule,
        HttpClientModule,
        BrowserAnimationsModule,
        ReactiveFormsModule,
        MatIconModule,
        TouchableComponentsModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [...getAppInitializerMockProvider('distribution-app')],
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
});
