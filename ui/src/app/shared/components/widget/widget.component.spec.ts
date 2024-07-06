import { CommonModule } from '@angular/common';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { WidgetComponent } from './widget.component';

describe('WidgetComponent', () => {
  let component: WidgetComponent;
  let fixture: ComponentFixture<WidgetComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [WidgetComponent],
      imports: [
        TreoCardModule,
        CommonModule,
        MaterialModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<WidgetComponent>(WidgetComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    // addRsaIconsMock(testContext);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
