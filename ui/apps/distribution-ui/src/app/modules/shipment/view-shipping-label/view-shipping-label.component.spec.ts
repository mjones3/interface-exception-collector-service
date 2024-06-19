import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { getAppInitializerMockProvider, RsaCommonsModule } from '@rsa/commons';
import { ViewShippingLabelComponent } from './view-shipping-label.component';

describe('ViewShippingLabelComponent', () => {
  let component: ViewShippingLabelComponent;
  let fixture: ComponentFixture<ViewShippingLabelComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ViewShippingLabelComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [
        RsaCommonsModule,
        BrowserAnimationsModule,
        RouterTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...getAppInitializerMockProvider('distribution-app'),
        {
          provide: ActivatedRoute,
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewShippingLabelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
