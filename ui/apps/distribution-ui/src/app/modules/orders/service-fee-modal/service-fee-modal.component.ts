import { Component, Inject, OnInit } from '@angular/core';
import { FormArray, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { LookUpDto, ValidationType } from '@rsa/commons';
import { OrderFee } from '@rsa/distribution/core/models/orders.model';

@Component({
  selector: 'rsa-service-fee-modal',
  templateUrl: './service-fee-modal.component.html',
})
export class ServiceFeeModalComponent implements OnInit {
  readonly validationType = ValidationType;

  addedFees: OrderFee[];
  serviceFeeGroup: FormGroup;
  serviceFeeArray: FormArray;
  serviceFees: LookUpDto[] = [];

  constructor(
    private fb: FormBuilder,
    private matDialogRef: MatDialogRef<ServiceFeeModalComponent>,
    @Inject(MAT_DIALOG_DATA)
    public data: {
      addedFees: OrderFee[];
      serviceFees: LookUpDto[];
    }
  ) {}

  ngOnInit(): void {
    this.addedFees = this.data.addedFees;
    this.serviceFees = this.data.serviceFees;
    this.serviceFeeGroup = this.fb.group({
      serviceFees: this.createFeesArray(this.addedFees || []),
    });
    this.serviceFeeArray = this.serviceFeeGroup.get('serviceFees') as FormArray;
  }

  saveFees() {
    if (this.serviceFeeGroup.value) {
      this.matDialogRef.close(this.serviceFeeArray.value);
    }
  }

  createFeesArray(value) {
    const currentValue: any[] = value && value?.length ? value : [];
    return this.fb.array(currentValue.map(val => this.createServiceFeeFormGroup(val)));
  }

  createServiceFeeFormGroup(value?: OrderFee): FormGroup {
    return this.fb.group({
      serviceFee: [value?.serviceFee || '', Validators.required],
      quantity: [value?.quantity ? +value.quantity : 0, [Validators.required, Validators.min(1)]],
    });
  }

  removeFeeClick(i: number) {
    this.serviceFeeArray.removeAt(i);
  }

  addFeeClick() {
    this.serviceFeeArray.push(this.createServiceFeeFormGroup());
  }
}
