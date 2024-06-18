import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { ActivatedRoute } from '@angular/router';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { getAppInitializerMockProvider, RsaCommonsModule } from '@rsa/commons';
import { ViewPackingListComponent } from './view-packing-list.component';

describe('ViewPackingListComponent', () => {
  let component: ViewPackingListComponent;
  let fixture: ComponentFixture<ViewPackingListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ViewPackingListComponent],
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
    fixture = TestBed.createComponent(ViewPackingListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
