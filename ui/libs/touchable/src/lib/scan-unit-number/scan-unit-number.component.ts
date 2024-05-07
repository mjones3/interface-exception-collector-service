import { Component, EventEmitter, Input, OnInit, Output, ViewChild } from '@angular/core';
import { FormBuilder, FormGroup, FormGroupDirective } from '@angular/forms';
import { extractUnitNumber, RsaValidators, ValidationType } from '@rsa/commons';

@Component({
  selector: 'rsa-scan-unit-number',
  exportAs: 'rsaScanUnitNumber',
  templateUrl: './scan-unit-number.component.html',
  styleUrls: ['./scan-unit-number.component.scss'],
})
export class ScanUnitNumberComponent implements OnInit {
  @Input() labelWidth: string;
  @Input() noShadow: boolean;
  @Input() labelTitle: string;
  @Output() scannedUnit: EventEmitter<any> = new EventEmitter<any>();
  @ViewChild(FormGroupDirective) formGroupDirective: FormGroupDirective;

  scanUnitNumberForm: FormGroup;
  readonly validationType = ValidationType;

  constructor(private formBuilder: FormBuilder) {}

  ngOnInit(): void {
    this.labelTitle = this.labelTitle && this.labelTitle !== '' ? this.labelTitle : 'unit-number.label';
    this.createScanUnitNumberForm();
  }

  createScanUnitNumberForm(): void {
    this.scanUnitNumberForm = this.formBuilder.group({
      scanUnitNumber: ['', { validators: [RsaValidators.unitNumber], updateOn: 'change' }],
    });
  }

  onFormSubmit() {
    const control = this.scanUnitNumberForm.controls['scanUnitNumber'];
    if (control.valid && control.value) {
      this.scannedUnit.emit(extractUnitNumber(control.value));
      setTimeout(() => {
        this.formGroupDirective.resetForm();
      });
    } else {
      this.scanUnitNumberForm.markAllAsTouched();
    }
  }
}
