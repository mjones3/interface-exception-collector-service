import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
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
import { CancelOrderModalComponent } from './cancel-order-modal.component';

describe('CancelOrderModalComponent', () => {
  let component: CancelOrderModalComponent;
  let fixture: ComponentFixture<CancelOrderModalComponent>;
  let matDialog: MatDialogRef<CancelOrderModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [CancelOrderModalComponent, ValidationPipe, ControlErrorsDirective, ModalTemplateComponent],
      imports: [
        FormsModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatSelectModule,
        MatIconModule,
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
        {provide: MatDialogRef, useClass: MatDialogRefMock},
        ...getAppInitializerMockProvider('distribution-app')
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<CancelOrderModalComponent>(CancelOrderModalComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    matDialog = TestBed.inject(MatDialogRef);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should confirm the cancellation when form is valid', () => {
    spyOn(matDialog, 'close');

    component.cancelOrderGroup.controls.reason.setValue(1);
    component.cancelOrder();

    expect(matDialog.close).toBeCalledWith(1);
  });
});
