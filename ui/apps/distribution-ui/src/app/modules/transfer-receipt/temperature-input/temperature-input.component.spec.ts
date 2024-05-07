import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { addRsaIconsMock, ControlErrorsDirective, ValidationPipe } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { createTestContext } from '@rsa/testing';
import { ThemeModule } from '@rsa/theme';

import { CommonModule } from '@angular/common';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { TemperatureInputComponent } from './temperature-input.component';

describe('TemperatureInputComponent', () => {
  let component: TemperatureInputComponent;
  let fixture: ComponentFixture<TemperatureInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [TemperatureInputComponent, ValidationPipe, ControlErrorsDirective],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MaterialModule,
        ThemeModule,
        CommonModule,
        BrowserAnimationsModule,
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
    fixture = TestBed.createComponent(TemperatureInputComponent);
    component = fixture.componentInstance;
    const testContext = createTestContext<TemperatureInputComponent>(TemperatureInputComponent);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
