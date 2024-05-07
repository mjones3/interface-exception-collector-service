import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormArray, FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  ControlErrorComponent,
  ControlErrorsDirective, getAppInitializerMockProvider,
  ModalTemplateComponent,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { OrderProduct } from '@rsa/distribution/core/models/orders.model';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import {
  antigensTestedMock,
  bloodTypesMock,
  productAttributesMock,
  productFamiliesMock,
} from '@rsa/distribution/data/mock/orders.mock';
import { createTestContext, MatDialogRefMock } from '@rsa/testing';
import { ToastrService } from 'ngx-toastr';
import { SelectButtonModule } from 'primeng/selectbutton';
import { AddProductModalComponent } from './add-product-modal.component';
import { MatCheckboxModule } from '@angular/material/checkbox';

describe('AddProductModalComponent', () => {
  let component: AddProductModalComponent;
  let fixture: ComponentFixture<AddProductModalComponent>;
  let toaster: ToastrService;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AddProductModalComponent, ValidationPipe, ControlErrorsDirective, ModalTemplateComponent, ControlErrorComponent],
      imports: [
        HttpClientTestingModule,
        SelectButtonModule,
        MatSelectModule,
        MatInputModule,
        MatFormFieldModule,
        MatDialogModule,
        MatCheckboxModule,
        MatIconModule,
        FormsModule,
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
        ...toasterMockProvider,
        FormBuilder,
        {provide: MatDialogRef, useClass: MatDialogRefMock},
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            familyCategory: '',
            currentProduct: null,
          },
        },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    const testContext = createTestContext<AddProductModalComponent>(AddProductModalComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    toaster = TestBed.inject(ToastrService);
    addRsaIconsMock(testContext);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create blood type and quantity controls whithout current product', () => {
    const bloodTypes = bloodTypesMock;
    const results = component.createBloodTypeAndQuantityArray(bloodTypes);
    expect(results).toBeInstanceOf(FormArray);
    expect(results).toHaveLength(8);
    results.controls.forEach((group: FormGroup) => {
      expect(group.controls.bloodType).toBeTruthy();
      expect(group.controls.quantity.value).not.toBeTruthy();
      expect(group.controls.quantity.disabled).toBe(false);
    });
  });

  it('should create blood type and quantity controls set current product quantity', () => {
    const currentProduct: OrderProduct = {
      id: 1,
      quantity: 2,
      bloodType: {
        id: 7,
        productFamily: 'plasma',
        bloodTypeValue: 'ABN',
        orderNumber: 1,
        active: true,
        descriptionKey: 'AB NEg',
      },
      productAttributes: [...productAttributesMock.slice(0, 2)],
      productFamily: {...productFamiliesMock[0]},
      productComment: 'Comment',
      antigensTested: [...antigensTestedMock.slice(2, 5)],
    };
    component.data.currentProduct = currentProduct;
    const results = component.createBloodTypeAndQuantityArray(bloodTypesMock);
    expect(results).toBeInstanceOf(FormArray);
    expect(results).toHaveLength(8);
    results.controls.forEach((group: FormGroup) => {
      expect(group.controls.bloodType).toBeTruthy();
      if (group.controls.bloodType.value.id !== currentProduct.bloodType.id) {
        expect(group.controls.quantity.disabled).toBeTruthy();
      }
    });
    const quantityResults = results.controls.filter((group: FormGroup) => !!group.controls.quantity.value);
    expect(quantityResults).toHaveLength(1);
  });
});
