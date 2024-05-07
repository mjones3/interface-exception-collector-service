import { Directive, ElementRef, HostListener, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';
import { BarcodeService } from '../../shared/services/barcode.service';
import { commonRegex } from '../../shared/utils/utils';

@Directive({
  selector: '[rsaFullProductCode]',
  exportAs: 'rsaFullProductCode',
})
export class FullProductCodeDirective {
  constructor(
    private barcodeService: BarcodeService,
    private elRef: ElementRef<HTMLInputElement>,
    @Optional() private ngControl: NgControl
  ) {}

  @HostListener('keydown.Tab', ['$event'])
  @HostListener('keydown.Enter', ['$event'])
  onBlurOrEnter() {
    if (!this.isValidFullProductCode()) {
      return;
    }

    const originalFullProdCode = this.control ? this.control.value : this.elRef.nativeElement.value;
    if (new RegExp(commonRegex.scannedProductCode).test(originalFullProdCode)) {
      this.barcodeService.getBarcodeTranslation(originalFullProdCode).subscribe(barcodeTranslationRes => {
        this.updateInputValue(barcodeTranslationRes?.body?.barcodeTranslation?.productCode);
      });
    }
  }

  @HostListener('keyup', ['$event'])
  onKeydown() {
    const originalValue = this.control ? this.control.value : this.elRef.nativeElement.value;
    if(!originalValue || originalValue === '') {
      return;
    }
    this.updateInputValueToUppercase(originalValue);
  }

  private updateInputValueToUppercase(prodCode: string) {
    if (this.control) {
      this.control.setValue(prodCode.toUpperCase());
    } else {
      this.elRef.nativeElement.value = prodCode.toUpperCase();
    }
  }

  isValidFullProductCode() {
    return this.control ? this.control.valid && this.control.value : true;
  }

  private updateInputValue(prodCode: string) {
    if (this.control) {
      this.control.setValue(prodCode);
    } else {
      this.elRef.nativeElement.value = prodCode;
    }
  }

  get control() {
    return this.ngControl ? this.ngControl.control : null;
  }
}
