import { HttpClientModule } from '@angular/common/http';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { getAppInitializerMockProvider, RsaCommonsModule, toasterMockProvider } from '@rsa/commons';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { EnterUnitNumberProductCodeComponent } from './enter-unit-number-product-code.component';

describe('EnterUnitNumberProductCodeComponent', () => {
  let component: EnterUnitNumberProductCodeComponent;
  let fixture: ComponentFixture<EnterUnitNumberProductCodeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [EnterUnitNumberProductCodeComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        SharedModule,
        RsaCommonsModule,
        HttpClientModule,
        FormsModule,
        BrowserAnimationsModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [...toasterMockProvider, ...getAppInitializerMockProvider('distribution-app')],
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
