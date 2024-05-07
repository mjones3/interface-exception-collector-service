import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { Option, ReasonDto, ValidationType } from '@rsa/commons';

@Component({
  selector: 'rsa-close-order-modal',
  templateUrl: './close-order-modal.component.html',
})
export class CloseOrderModalComponent implements OnInit {
  readonly validationType = ValidationType;
  closeOrderGroup: FormGroup;
  closeOrderReasons: ReasonDto[] = [];

  constructor(private fb: FormBuilder, private matDialogRef: MatDialogRef<CloseOrderModalComponent>) {}

  ngOnInit(): void {
    const defaultReason = this.closeOrderReasons[0]?.id;
    this.closeOrderGroup = this.fb.group({
      reason: [defaultReason, Validators.required],
    });
  }

  closeOrder() {
    if (this.closeOrderGroup.valid) {
      this.matDialogRef.close(this.closeOrderGroup.value?.reason);
    }
  }
}
