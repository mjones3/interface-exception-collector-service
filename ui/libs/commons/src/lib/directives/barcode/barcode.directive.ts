import { Directive, ElementRef, HostListener, Optional } from '@angular/core';
import { NgControl } from '@angular/forms';
import { BarcodeParts } from '../../shared/models/barcode-parts.model';
import { BarcodeService } from '../../shared/services/barcode.service';
import { extractUnitNumber } from '../../shared/utils/utils';

@Directive({
  selector: '[rsaBarcode]',
  exportAs: 'rsaBarcode',
})
export class BarcodeDirective {
  constructor(
    private barcodeService: BarcodeService,
    private elRef: ElementRef<HTMLInputElement>,
    @Optional() private ngControl: NgControl
  ) {}

  @HostListener('keydown.Tab', ['$event'])
  @HostListener('keydown.Enter', ['$event'])
  onBlurOrEnter() {
    if (!this.isValidBarcode()) {
      return;
    }
    const originalBarcode = this.control ? this.control.value : this.elRef.nativeElement.value;
    const unitNumber = extractUnitNumber(originalBarcode);
    if (unitNumber) {
      this.updateInputValue({ barcode: unitNumber });
      return;
    }
    this.barcodeService.getBarcodeParts(originalBarcode).subscribe(barcodeParts => {
      this.updateInputValue(barcodeParts);
    });
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
