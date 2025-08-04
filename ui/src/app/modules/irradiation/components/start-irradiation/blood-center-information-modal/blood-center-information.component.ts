import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {ActionButtonComponent} from "../../../../../shared/components/buttons/action-button.component";
import { MatSelectModule } from '@angular/material/select';
import { CommonModule } from '@angular/common';

export interface ImportDetailsData {
  bloodCenterName: string;
  address: string;
  registrationNumber: string;
  licenseNumber?: string;
}

@Component({
  selector: 'biopro-blood-center-information',
  templateUrl: './blood-center-information.component.html',
  styleUrls: ['./blood-center-information.component.scss'],
  standalone: true,
  imports: [
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    ReactiveFormsModule,
    CommonModule,
    ActionButtonComponent
  ]
})
export class ImportDetailsModal {
  private dialogRef = inject(MatDialogRef<ImportDetailsModal>);
  private fb = inject(FormBuilder);

  importForm: FormGroup = this.fb.group({
    bloodCenterName: ['', Validators.required],
    address: ['', Validators.required],
    registrationNumber: ['', Validators.required],
    licenseStatus: ['', Validators.required],
    licenseNumber: ['']
  });

  constructor() {
    this.importForm.get('licenseStatus')?.valueChanges.subscribe(value => {
      const licenseNumberControl = this.importForm.get('licenseNumber');
      if (value === 'isLicensed') {
        licenseNumberControl?.setValidators([Validators.required]);
      } else {
        licenseNumberControl?.clearValidators();
      }
      licenseNumberControl?.updateValueAndValidity();
    });
  }
  get isLicensed(): boolean {
    return this.importForm.get('licenseStatus')?.value === 'isLicensed';
  }

  submit() {
    if (this.importForm.valid) {
      const formValue = { ...this.importForm.value };
      
      if (formValue.licenseStatus !== 'isLicensed') {
        delete formValue.licenseNumber;
      }
      
      delete formValue.licenseStatus;
      this.dialogRef.close(formValue);
    }
  }

  cancel() {
    this.dialogRef.close(null);
  }
}
