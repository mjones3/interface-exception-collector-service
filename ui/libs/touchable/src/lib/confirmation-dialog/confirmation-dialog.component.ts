import { Component, Input, OnInit, TemplateRef } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { ValidationType, WhiteSpaceValidator } from '@rsa/commons';
import { InputType } from '../../shared/models/input-type.enum';

@Component({
  selector: 'rsa-confirmation-dialog',
  exportAs: 'rsaConfirmationDialog',
  templateUrl: './confirmation-dialog.component.html',
  styleUrls: ['./confirmation-dialog.component.scss'],
})
export class ConfirmationDialogComponent implements OnInit {
  @Input() commentsModal = false;
  @Input() showAction = true;
  @Input() showCancelButton = true;
  @Input() commentsLabel = 'comments.label';
  @Input() iconName: string;
  @Input() dialogTitle = 'Confirmation Dialog Title';
  @Input() dialogText = 'This is a warning confirmation text, do you want to continue?';
  @Input() acceptBtnTittle = 'submit.label';
  @Input() cancelBtnTittle = 'cancel.label';
  @Input() dialogClasses = 'min-w-100';
  @Input() contentClasses = '';
  @Input() contentTemplate: TemplateRef<Element>;
  @Input() commentsMaxLength = 2000;

  confirmationForm: FormGroup;
  readonly inputType = InputType;
  readonly validationType = ValidationType;

  constructor(private _formBuilder: FormBuilder) {}

  ngOnInit(): void {
    if (this.commentsModal) {
      this.createConfirmationForm();
    }
  }

  createConfirmationForm(): void {
    this.confirmationForm = this._formBuilder.group({
      comment: ['', WhiteSpaceValidator.validate],
    });
  }
}
