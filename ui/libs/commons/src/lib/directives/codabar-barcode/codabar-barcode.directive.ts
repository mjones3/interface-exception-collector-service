import { Directive, ElementRef, HostListener, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';
import { BarcodeParts } from '../../shared/models/barcode-parts.model';
import { commonRegex } from '../../shared/utils/utils';

@Directive({
  selector: '[rsaCodabarBarcode]',
  exportAs: 'rsaCodabarBarcode',
})
export class CodabarBarcodeDirective {
  constructor(private elRef: ElementRef<HTMLInputElement>, @Optional() private ngControl: NgControl) {
    this.loadMaps();
  }
  codabarFirstCharMap;
  codabarSecondCharMap;

  loadMaps() {
    this.codabarFirstCharMap = new Map();
    this.codabarFirstCharMap.set(0, 'C');
    this.codabarFirstCharMap.set(1, 'E');
    this.codabarFirstCharMap.set(2, 'F');
    this.codabarFirstCharMap.set(3, 'G');
    this.codabarFirstCharMap.set(4, 'H');
    this.codabarFirstCharMap.set(5, 'J');
    this.codabarFirstCharMap.set(6, 'K');
    this.codabarFirstCharMap.set(7, 'L');
    this.codabarFirstCharMap.set(8, 'M');
    this.codabarFirstCharMap.set(9, 'N');
    this.codabarFirstCharMap.set(10, 'P');
    this.codabarFirstCharMap.set(11, 'Q');
    this.codabarFirstCharMap.set(12, 'R');
    this.codabarFirstCharMap.set(13, 'S');
    this.codabarFirstCharMap.set(14, 'T');
    this.codabarFirstCharMap.set(15, 'V');
    this.codabarFirstCharMap.set(16, 'W');
    this.codabarFirstCharMap.set(17, 'X');
    this.codabarFirstCharMap.set(18, 'Y');
    this.codabarFirstCharMap.set(19, 'Z');

    this.codabarSecondCharMap = new Map();
    this.codabarSecondCharMap.set(0, '');
    this.codabarSecondCharMap.set(1, 'F');
    this.codabarSecondCharMap.set(2, 'G');
    this.codabarSecondCharMap.set(3, 'K');
    this.codabarSecondCharMap.set(4, 'L');
  }

  getAlphas(barcodeNumber: string): string {
    const number = barcodeNumber.substring(1, 3);
    const firstChar = parseInt(number, 10) % 20;
    const secondChar = Math.floor(parseInt(number, 10) / 20);
    return this.codabarSecondCharMap.get(secondChar) + this.codabarFirstCharMap.get(firstChar);
  }

  @HostListener('keydown.Tab', ['$event'])
  @HostListener('keydown.Enter', ['$event'])
  onBlurOrEnter() {
    if (!this.isValidBarcode()) {
      return;
    }
    const originalBarcode = this.control ? this.control.value : this.elRef.nativeElement.value;
    if (!new RegExp(commonRegex.codabarUnitNumber, 'g').test(originalBarcode)) {
      return originalBarcode;
    }
    const codabarUnitNumber = this.getAlphas(originalBarcode) + originalBarcode.substring(3, 8);
    this.updateInputValue({ barcode: codabarUnitNumber });
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
