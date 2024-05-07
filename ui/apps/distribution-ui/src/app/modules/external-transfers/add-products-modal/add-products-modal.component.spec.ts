import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DragDropModule } from '@angular/cdk/drag-drop';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { FormBuilder, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialog, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { TreoScrollbarDirective, TreoScrollbarMock, TreoScrollbarModule } from '@treo';
import { SelectButtonModule } from 'primeng/selectbutton';
import { AddProductsModalComponent } from './add-products-modal.component';
import { getAppInitializerMockProvider, RsaCommonsModule } from '@rsa/commons';

describe('AddProductsModalComponent', () => {
  let component: AddProductsModalComponent;
  let fixture: ComponentFixture<AddProductsModalComponent>;
  let matDialog: MatDialog;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddProductsModalComponent],
      imports: [
        HttpClientTestingModule,
        SelectButtonModule,
        MatSelectModule,
        MatInputModule,
        MatFormFieldModule,
        MatListModule,
        RsaCommonsModule,
        TreoScrollbarModule,
        MatDialogModule,
        MatIconModule,
        FormsModule,
        ReactiveFormsModule,
        BrowserAnimationsModule,
        DragDropModule,
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
            unitNumber: 'W123456789123',
          },
        },
      ],
    })
      .overrideModule(TreoScrollbarModule, {
        remove: { declarations: [TreoScrollbarDirective], exports: [TreoScrollbarDirective] },
        add: { declarations: [TreoScrollbarMock], exports: [TreoScrollbarMock] },
      })
      .compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<AddProductsModalComponent>(AddProductsModalComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    matDialog = TestBed.inject(MatDialog);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
