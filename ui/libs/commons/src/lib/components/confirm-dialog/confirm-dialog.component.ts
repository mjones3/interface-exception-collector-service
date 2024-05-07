import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { ValidationType } from '../../pipes/validation.pipe';

@Component({
  selector: 'rsa-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
})
export class ConfirmDialogComponent implements OnInit {
  @Input() title = 'confirmation.label';
  @Input() cancelTitle = 'cancel.label';
  @Input() acceptTitle = 'confirm.label';
  @Input() confirmMessageTpl: TemplateRef<any>;
  @Input() confirmMessage = '';
  @Input() enableComment = false;
  @Input() isRequiredComment = false;
  @Input() commentLabel = 'comment.label';
  @Input() commentInputRows = 3;
  @Input() maxCharacters = 500;

  readonly validationType = ValidationType;
  confirmGroup: FormGroup;

  constructor(private matDialogRef: MatDialogRef<any>, private fb: FormBuilder) {}

  ngOnInit(): void {
    this.confirmGroup = this.fb.group({
      comment: ['', this.isRequiredComment ? Validators.required : ''],
    });
  }

  confirm() {
    if (this.enableComment) {
      if ((this.isRequiredComment && this.confirmGroup.valid) || !this.isRequiredComment) {
        this.matDialogRef.close(this.confirmGroup.value);
      } else {
        this.confirmGroup.markAllAsTouched();
      }
    } else {
      this.matDialogRef.close(true);
    }
  }
}
