import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { getAppInitializerMockProvider, RsaCommonsModule } from '@rsa/commons';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ServiceFeeModalComponent } from './service-fee-modal.component';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';

describe('ServiceFeeModalComponent', () => {
  let component: ServiceFeeModalComponent;
  let fixture: ComponentFixture<ServiceFeeModalComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ServiceFeeModalComponent],
      imports: [
        MatDialogModule,
        MatIconModule,
        MatFormFieldModule,
        MatSelectModule,
        RsaCommonsModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...getAppInitializerMockProvider('distribution-app'),
        FormBuilder,
        { provide: MatDialogRef, useClass: MatDialogRefMock },
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            addedFees: [],
            serviceFees: [],
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<ServiceFeeModalComponent>(ServiceFeeModalComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
