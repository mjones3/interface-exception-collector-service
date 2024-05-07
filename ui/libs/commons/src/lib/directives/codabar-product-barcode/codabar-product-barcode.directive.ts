import { Directive, ElementRef, HostListener, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';
import { BarcodeParts } from '../../shared/models/barcode-parts.model';
import { commonRegex } from '../../shared/utils/utils';

@Directive({
  selector: '[rsaCodabarProductBarcode]',
  exportAs: 'rsaCodabarProductBarcode',
})
export class CodabarProductBarcodeDirective {
  constructor(private elRef: ElementRef<HTMLInputElement>, @Optional() private ngControl: NgControl) {}

  @HostListener('keydown.Tab', ['$event'])
  @HostListener('keydown.Enter', ['$event'])
  onBlurOrEnter() {
    if (!this.isValidBarcode()) {
      return;
    }
    const originalBarcode = this.control ? this.control.value : this.elRef.nativeElement.value;
    if (!new RegExp(commonRegex.codabarProductCode, 'g').test(originalBarcode)) {
      return;
    }
    const codabarProductCode = originalBarcode.substring(2, 7);
    this.updateInputValue({ barcode: codabarProductCode });
  }

  isValidBarcode() {
    return this.control ? this.control.valid && this.control.value : true;
  }

  private updateInputValue(barcodeParts: BarcodeParts) {
    if (this.control) {
      this.control.setValue(barcodeParts.barcode);
    } else {
      this.elRef.nativeElement.value = barcodeParts.barcode;
    }
  }

  get control() {
    return this.ngControl ? this.ngControl.control : null;
  }
}
