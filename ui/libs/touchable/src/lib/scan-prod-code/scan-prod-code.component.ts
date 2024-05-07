import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective } from '@angular/forms';
import { extractProductCode, RsaValidators, ValidationType } from '@rsa/commons';

@Component({
  selector: 'rsa-scan-prod-code',
  exportAs: 'rsaScanProdCode',
  templateUrl: './scan-prod-code.component.html',
})
export class ScanProdCodeComponent implements OnInit {
  @Input() labelWidth: string;
  @Input() noShadow: boolean;
  @Input() labelTitle: string;
  @Output() scannedProductCode: EventEmitter<any> = new EventEmitter<any>();
  @ViewChild(FormGroupDirective) formGroupDirective: FormGroupDirective;

  scanProductCodeForm: FormGroup;
  readonly validationType = ValidationType;

  constructor(private formBuilder: FormBuilder) {}

  ngOnInit(): void {
    this.labelTitle = this.labelTitle && this.labelTitle !== '' ? this.labelTitle : 'product-code.label';
    this.createScanProdCodeForm();
  }

  createScanProdCodeForm(): void {
    this.scanProductCodeForm = this.formBuilder.group({
      prodCode: ['', { validators: [RsaValidators.fullProductCode], updateOn: 'change' }],
    });
  }

  onFormSubmit() {
    const control = this.scanProductCodeForm.controls['prodCode'];
    if (control.valid && control.value) {
      this.scannedProductCode.emit(extractProductCode(control.value));
      setTimeout(() => {
        this.formGroupDirective.resetForm();
      });
    } else {
      this.scanProductCodeForm.markAllAsTouched();
    }
  }
}
