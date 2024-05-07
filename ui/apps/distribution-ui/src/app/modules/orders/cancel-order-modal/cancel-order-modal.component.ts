import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { ReasonDto, ValidationType } from '@rsa/commons';

@Component({
  selector: 'rsa-cancel-order-modal',
  templateUrl: './cancel-order-modal.component.html',
})
export class CancelOrderModalComponent implements OnInit {
  readonly validationType = ValidationType;
  cancelOrderGroup: FormGroup;
  cancellationReasons: ReasonDto[] = [];

  constructor(private fb: FormBuilder, private matDialogRef: MatDialogRef<CancelOrderModalComponent>) {
    this.cancelOrderGroup = fb.group({
      reason: ['', Validators.required],
    });
  }

  ngOnInit(): void {}

  cancelOrder() {
    if (this.cancelOrderGroup.valid) {
      this.matDialogRef.close(this.cancelOrderGroup.value?.reason);
    }
  }
}
