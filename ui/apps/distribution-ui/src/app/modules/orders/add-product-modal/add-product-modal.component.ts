import { HttpResponse } from '@angular/common/http';
import { Component, Inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { MatCheckboxChange } from '@angular/material/checkbox';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSelectChange } from '@angular/material/select';
import {
  OrderBloodTypeDto,
  OrderProductAttributeDto,
  OrderProductAttributeOptionDto,
  OrderProductFamilyDto,
  OrderService,
  ValidationType,
} from '@rsa/commons';
import { ANTIGEN_TESTED, BloodTypeAndQuantity, OrderProduct } from '@rsa/distribution/core/models/orders.model';
import { ToastrService } from 'ngx-toastr';
import { of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'rsa-add-product-modal',
  templateUrl: './add-product-modal.component.html',
  styleUrls: ['./add-product-modal.component.scss'],
})
export class AddProductModalComponent implements OnInit {
  readonly validationType = ValidationType;

  productGroup: FormGroup;
  productAttributes: OrderProductAttributeDto[];
  productAttributesArray: FormArray;
  bloodTypeAndQuantityArray: FormArray;
  bloodTypes: OrderBloodTypeDto[];
  productFamilies: OrderProductFamilyDto[];
  maxChars = 1000;
  antigensTested: OrderProductAttributeOptionDto[];
  showAntigensTestedControl: boolean;

  constructor(
    private fb: FormBuilder,
    private matDialogRef: MatDialogRef<AddProductModalComponent>,
    private orderService: OrderService,
    private toasterService: ToastrService,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      familyCategory: string;
      currentProduct: OrderProduct;
      productAttributes: OrderProductAttributeDto[];
    }
  ) {}

  ngOnInit(): void {
    this.createForm();

    this.orderService
      .getOrderProductFamiliesByCriteria({ familyCategory: this.data.familyCategory })
      .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderProductFamilyDto[]>)))
      .subscribe(productFamilies => {
        this.productFamilies = productFamilies.body;
        this.productAttributes = this.data.productAttributes;
        this.antigensTested = this.productAttributes.find(attr => attr.propertyKey === ANTIGEN_TESTED).attributeOptions;

        if (this.data.currentProduct) {
          if (this.existAntigenTestedOnCurrentProd()) {
            this.addOrRemoveAntigensTested(true);
          }
          this.setInitialFormValues();
          this.loadBloodTypes(this.data.currentProduct.productFamily.familyValue);
        }
      });
  }

  selectedFamilyProduct(event: MatSelectChange) {
    this.loadBloodTypes(event.value.familyValue);
  }

  createProdAttributesArray(value) {
    const currentValue: OrderProductAttributeDto[] = value && value?.length ? value : [];
    return this.fb.array(
      currentValue.map(prodAttr => {
        return this.createProdAttributeControl(prodAttr);
      })
    );
  }

  onCheckboxChange(event: MatCheckboxChange) {
    if (event.checked) {
      this.productAttributesArray.push(new FormControl(event.source.value));
    } else {
      const index = this.productAttributesArray.controls.findIndex(x => x.value === event.source.value);
      this.productAttributesArray.removeAt(index);
    }
    const existAntigenAttr = this.productAttributesArray.getRawValue().some(attr => this.isAntigenTestedSelected(attr));
    this.addOrRemoveAntigensTested(existAntigenAttr);
  }

  isProdAttrChecked(prodAttr: OrderProductAttributeDto) {
    return this.productAttributesArray.getRawValue().some((attr: OrderProductAttributeDto) => prodAttr.id === attr.id);
  }

  createBloodTypeAndQuantityArray(value): FormArray {
    const currentValue: OrderBloodTypeDto[] = value && value?.length ? value : [];
    return this.fb.array(
      currentValue.map(bloodType => {
        const bloodTypeQuantity =
          this.data.currentProduct?.bloodType &&
          this.data.currentProduct?.bloodType.id === bloodType.id &&
          this.data.currentProduct?.quantity
            ? this.data.currentProduct?.quantity
            : null;
        const disableQuantityField = !!this.data.currentProduct && !bloodTypeQuantity;
        return this.createBloodTypeAndQuantityControl(bloodType, bloodTypeQuantity, disableQuantityField);
      })
    );
  }

  isAntigenTestedSelected(attr: OrderProductAttributeDto): boolean {
    return attr?.propertyKey === ANTIGEN_TESTED;
  }

  existAntigenTestedOnCurrentProd(): OrderProductAttributeDto {
    return (
      this.data.currentProduct &&
      this.data.currentProduct.productAttributes.find((attr: OrderProductAttributeDto) =>
        this.isAntigenTestedSelected(attr)
      )
    );
  }

  createProdAttributeControl(value?: OrderProductAttributeDto): FormControl {
    return this.fb.control(value || null);
  }

  createBloodTypeAndQuantityControl(bloodType?: OrderBloodTypeDto, quantity?: number, disabled?: boolean): FormGroup {
    return this.fb.group({
      bloodType: [bloodType || {}],
      quantity: [{ value: quantity, disabled: disabled }, Validators.min(1)],
    });
  }

  addProduct() {
    if (this.productGroup.valid) {
      const bloodTypeAndQuantity = this.getBloodTypeAndQuantity();
      if (bloodTypeAndQuantity.length > 0) {
        this.matDialogRef.close({
          ...this.productGroup.value,
          productFamily: this.productGroup.controls.productFamily.value,
          bloodTypeAndQuantity: bloodTypeAndQuantity,
        });
      }
    }
  }

  getBloodTypeAndQuantity() {
    const btFormArray = this.productGroup.controls.bloodTypeAndQuantity as FormArray;
    return btFormArray.controls
      .map((controls: FormGroup) => {
        return {
          bloodType: controls.controls.bloodType.value,
          quantity: +controls.controls.quantity.value,
        };
      })
      .filter((btAndQuantity: BloodTypeAndQuantity) => btAndQuantity.quantity);
  }

  validateBloodTypeQuantity(): any {
    return (group: FormGroup): any => {
      const btFormArray = group.controls.bloodTypeAndQuantity as FormArray;
      if (btFormArray?.controls.length > 0) {
        const bloodTypeAndQuantity = this.getBloodTypeAndQuantity();
        if (bloodTypeAndQuantity.length === 0) {
          btFormArray.setErrors({ required: true });
        }
      }
      return;
    };
  }

  private createForm() {
    this.productGroup = this.fb.group({
      id: [this.data.currentProduct?.id || null],
      productFamily: ['', Validators.required],
      productAttributes: this.createProdAttributesArray(this.data.currentProduct?.productAttributes || []),
      bloodTypeAndQuantity: new FormArray([]),
      productComment: ['', Validators.maxLength(this.maxChars)],
    });
    this.productAttributesArray = this.productGroup.get('productAttributes') as FormArray;
    this.bloodTypeAndQuantityArray = this.productGroup.get('bloodTypeAndQuantity') as FormArray;
    this.productGroup.setValidators(this.validateBloodTypeQuantity());
  }

  private loadBloodTypes(familyValue: string) {
    this.orderService
      .getOrderBloodTypeByCriteria({ productFamily: familyValue })
      .pipe(map(response => response.body))
      .subscribe(bloodTypes => {
        this.bloodTypes = bloodTypes;
        this.productGroup.removeControl('bloodTypeAndQuantity');
        this.productGroup.addControl('bloodTypeAndQuantity', this.createBloodTypeAndQuantityArray(this.bloodTypes));
        this.bloodTypeAndQuantityArray = this.productGroup.get('bloodTypeAndQuantity') as FormArray;
      });
  }

  private addOrRemoveAntigensTested(showAntigensTested: boolean) {
    if (showAntigensTested) {
      this.showAntigensTestedControl = true;
      this.productGroup.addControl('antigensTested', new FormControl([], Validators.required));
    } else {
      this.showAntigensTestedControl = false;
      this.productGroup.removeControl('antigensTested');
    }
  }

  private setInitialFormValues() {
    let antigensSelected = [];
    if (this.data.currentProduct && this.data.currentProduct.antigensTested?.length) {
      antigensSelected = this.antigensTested.filter(antigen =>
        this.data.currentProduct.antigensTested.some(
          (an: OrderProductAttributeOptionDto) => an.optionValue === antigen.optionValue
        )
      );
    }
    this.productGroup.patchValue({
      productFamily: this.productFamilies.find(
        prod => prod.familyValue === this.data.currentProduct.productFamily?.familyValue
      ),
      productComment: this.data.currentProduct.productComment,
      antigensTested: antigensSelected,
    });

    this.productGroup.get('productFamily').disable();
  }

  showCanceledChangesToasterMessage() {
    if (this.data.currentProduct) {
      this.toasterService.warning('the-changes-have-been-cancelled.label');
    }
  }
}
