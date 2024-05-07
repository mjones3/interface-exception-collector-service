import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { TreoCardModule } from '@treo';
import { BaseProcessLayoutComponent } from './base-process-layout.component';

describe('BaseProcessLayoutComponent', () => {
  let component: BaseProcessLayoutComponent;
  let fixture: ComponentFixture<BaseProcessLayoutComponent>;

  beforeEach(
    waitForAsync(() => {
      TestBed.configureTestingModule({
        imports: [
          TreoCardModule,
          MaterialModule,
          TranslateModule.forRoot({
            loader: {
              provide: TranslateLoader,
              useClass: TranslateFakeLoader,
            },
          }),
          NoopAnimationsModule,
          RouterTestingModule,
          RsaCommonsModule,
        ],
      }).compileComponents();
    })
  );

  beforeEach(() => {
    const testContext = createTestContext<BaseProcessLayoutComponent>(BaseProcessLayoutComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
