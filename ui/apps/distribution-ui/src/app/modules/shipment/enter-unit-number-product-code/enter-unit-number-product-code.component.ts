import { ChangeDetectorRef, Component, EventEmitter, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { RsaValidators, ValidationType, VerifyFilledProductDto } from '@rsa/commons';
import { ScanUnitNumberCheckDigitComponent } from '@rsa/distribution/shared/components/scan-unit-number-check-digit/scan-unit-number-check-digit.component';

@Component({
  selector: 'rsa-enter-unit-number-product-code',
  templateUrl: './enter-unit-number-product-code.component.html',
})
export class EnterUnitNumberProductCodeComponent implements OnInit {
  disableCheckDigit = true;
  productGroup: FormGroup;
  unitNumberFocus = true;
  readonly validationType = ValidationType;

  @Output() unitNumberProductCodeSelected: EventEmitter<VerifyFilledProductDto> = new EventEmitter<
    VerifyFilledProductDto
  >();

  @ViewChild('unitnumber') unitNumberComponent: ScanUnitNumberCheckDigitComponent;

  constructor(protected fb: FormBuilder, private changeDetector: ChangeDetectorRef) {
    this.productGroup = fb.group({
      unitNumber: ['', [Validators.required, RsaValidators.unitNumber]],
      productCode: ['', [RsaValidators.fullProductCode, Validators.required]],
      visualInspection: ['', [Validators.required]],
    });
  }

  ngOnInit(): void {}

  verifyUnit(event: { unitNumber: string; checkDigit: string; scanner: boolean }) {
    this.productGroup.controls.unitNumber.setValue(event.unitNumber);
  }

  onSelectVisualInspection(): void {
    if (this.productGroup.valid) {
      const visualInspection = this.productGroup.controls.visualInspection.value;
      if (visualInspection === 'satisfactory') {
        setTimeout(() => {
          this.verifyProduct();
        }, 300);
      }
    }
  }

  verifyProduct(): void {
    this.unitNumberProductCodeSelected.emit({
      ...this.productGroup.value,
      productCode: this.productCode,
    });
  }

  disableProductGroup(): void {
    this.productGroup.disable();
    this.unitNumberComponent.controlUnitNumber.disable();
  }

  resetProductFormGroup(): void {
    this.productGroup.controls.visualInspection.setValue(null);
    this.productGroup.reset();
    this.unitNumberComponent.reset();
    this.productGroup.updateValueAndValidity();
    this.changeDetector.detectChanges();
  }

  get unitNumber(): string {
    return this.productGroup.controls.unitNumber.value;
  }

  get productCode(): string {
    let productCode: string = this.productGroup.controls.productCode.value ?? '';
    const scanner = productCode.startsWith('=<');
    if (productCode && scanner) {
      if (scanner) {
        productCode = productCode.substring(2);
      }
      return productCode;
    }
    return this.productGroup.controls.productCode.value;
  }

  get enableVisualInspection(): boolean {
    return this.productGroup.controls.unitNumber.valid && this.productGroup.controls.productCode.valid;
  }
}
