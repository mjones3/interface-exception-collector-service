import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  ControlErrorsDirective, getAppInitializerMockProvider,
  ModalTemplateComponent,
  ValidationPipe,
} from '@rsa/commons';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { SelectButtonModule } from 'primeng/selectbutton';
import { CloseOrderModalComponent } from './close-order-modal.component';

describe('CloseOrderModalComponent', () => {
  let component: CloseOrderModalComponent;
  let fixture: ComponentFixture<CloseOrderModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CloseOrderModalComponent, ModalTemplateComponent, ControlErrorsDirective, ValidationPipe],
      imports: [
        MatSelectModule,
        MatFormFieldModule,
        MatDialogModule,
        MatIconModule,
        FormsModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        SelectButtonModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        { provide: MatDialogRef, useClass: MatDialogRefMock },
        ...getAppInitializerMockProvider('distribution-app'),
        FormBuilder,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<CloseOrderModalComponent>(CloseOrderModalComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addRsaIconsMock(testContext);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
