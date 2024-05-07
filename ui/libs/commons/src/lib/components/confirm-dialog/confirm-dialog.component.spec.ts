import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { ControlErrorsDirective, ValidationPipe } from '@rsa/commons';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { addRsaIconsMock } from '../../data/mock/icons.mock';
import { ModalTemplateComponent } from '../modal-template/modal-template.component';
import { ConfirmDialogComponent } from './confirm-dialog.component';

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;
  let fixture: ComponentFixture<ConfirmDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConfirmDialogComponent, ModalTemplateComponent, ValidationPipe, ControlErrorsDirective],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MatFormFieldModule,
        MatDialogModule,
        MatIconModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [{ provide: MatDialogRef, useClass: MatDialogRefMock }],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<ConfirmDialogComponent>(ConfirmDialogComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addRsaIconsMock(testContext);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
