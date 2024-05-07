import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { DescriptionCardComponent } from './description-card.component';

describe('DescriptionCardComponent', () => {
  let component: DescriptionCardComponent;
  let fixture: ComponentFixture<DescriptionCardComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        declarations: [DescriptionCardComponent],
        imports: [
          MaterialModule,
          NoopAnimationsModule,
          TreoCardModule,
          TranslateModule.forRoot({
            loader: {
              provide: TranslateLoader,
              useClass: TranslateFakeLoader,
            },
          }),
        ],
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
