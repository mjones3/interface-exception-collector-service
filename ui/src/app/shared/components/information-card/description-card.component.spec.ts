import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { createTestContext } from 'app/core/test/test-context';
import { DescriptionCardComponent } from './description-card.component';

describe('DescriptionCardComponent', () => {
  let component: DescriptionCardComponent;
  let fixture: ComponentFixture<DescriptionCardComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [DescriptionCardComponent],
        imports: [
          NoopAnimationsModule,
          TranslateModule.forRoot({
            loader: {
              provide: TranslateLoader,
              useClass: TranslateFakeLoader,
            },
          }),
        ],
        providers: [
          provideHttpClient(),
          provideHttpClientTesting()
        ]
      }).compileComponents();
    })
  );

  beforeEach(() => {
    const testContext = createTestContext<DescriptionCardComponent>(DescriptionCardComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    component.descriptions = [
      {
        label: 'Unit Number',
        value: '',
      },
    ];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
