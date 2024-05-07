import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';

@Component({
  selector: 'rsa-common-modal-template',
  templateUrl: './modal-template.component.html',
})
export class ModalTemplateComponent implements OnInit {
  @Input() title: string;
  @Input() acceptTitle: string;
  @Input() acceptBtnClass = 'bg-gray-light';
  @Input() cancelTitle: string;
  @Input() cancelBtnClass = 'bg-gray-light';
  @Input() isValid = true;
  @Input() displayActionButtons = true;
  @Input() displayCloseButton = true;
  @Input() dialogContentAlign = 'text-center';

  @Output() acceptSelection = new EventEmitter<boolean>();

  constructor(private matDialog: MatDialog) {}

  ngOnInit(): void {}

  submit() {
    this.matDialog.closeAll();
  }

  accept() {
    this.acceptSelection.emit(true);
  }
}
