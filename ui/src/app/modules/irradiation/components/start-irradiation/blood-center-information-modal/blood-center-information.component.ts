import { Component, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import {ActionButtonComponent} from "../../../../../shared/components/buttons/action-button.component";

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
    ReactiveFormsModule,
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
    licenseNumber: ['']
  });

  submit() {
    if (this.importForm.valid) {
      this.dialogRef.close(this.importForm.value);
    }
  }

  cancel() {
    this.dialogRef.close(null);
  }
}
