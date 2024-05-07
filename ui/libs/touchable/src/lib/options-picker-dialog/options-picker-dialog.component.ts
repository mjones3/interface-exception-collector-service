import {
  Component,
  EventEmitter,
  forwardRef,
  Inject,
  Input,
  OnChanges,
  OnInit,
  Output,
  TemplateRef,
  ViewEncapsulation,
} from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { BaseControlValueAccessor, Option } from '@rsa/commons';

@Component({
  selector: 'rsa-options-picker-dialog',
  exportAs: 'rsaOptionsPickerDialog',
  templateUrl: './options-picker-dialog.component.html',
  styleUrls: ['./options-picker-dialog.component.scss'],
  encapsulation: ViewEncapsulation.None,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => OptionsPickerDialogComponent), multi: true }],
})
export class OptionsPickerDialogComponent extends BaseControlValueAccessor<string> implements OnInit, OnChanges {
  @Input() headerTitle: string;
  @Input() options: any;
  @Input() optionsLabel: string;
  @Input() showCloseButton = true;
  @Input() optionDescriptionTpl: TemplateRef<any>;

  @Output() optionChange = new EventEmitter<Option>();
  selectedOption: Option;
  columns: number;

  ngOnChanges(changes: any) {
    console.log(changes);
  }

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: any,
    private dialogRef: MatDialogRef<OptionsPickerDialogComponent>
  ) {
    super();
  }

  ngOnInit(): void {
    if (this.data.options) {
      this.options = this.data.options;
      this.optionsLabel = this.data.optionsLabel;
      this.headerTitle = this.data.dialogTitle;
      this.columns = this.options.length > 4 ? 2 : 1;
    }
  }

  writeValue(value: string): void {
    super.writeValue(value);
    // this.selectedOption = this.getOptionByValue(value ? value.toString() : null);
  }

  selectOption(selectedValue: Option) {
    this.selectedOption = selectedValue;
    // Emit an option change value
    this.optionChange.emit(this.selectedOption);
    // Emit onChange CVA
    this.value = selectedValue.selectionKey;
    this.onChange(this.value);
    this.dialogRef.close(selectedValue);
  }

  isSelected(val: Option): boolean {
    return this.selectedOption && val.selectionKey === this.selectedOption.selectionKey;
  }

  getColor(button: Option) {
    return this.isSelected(button) ? 'primary' : 'default';
  }
}
