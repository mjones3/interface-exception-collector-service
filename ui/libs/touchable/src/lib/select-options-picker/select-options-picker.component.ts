import {
  AfterViewInit,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Optional,
  Output,
  Self,
  SimpleChanges,
  ViewChild,
  ViewEncapsulation,
} from '@angular/core';
import { FormBuilder, FormControl, NgControl } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { AutoUnsubscribe, ControlValueAccessorWithValidator, Pageable } from '@rsa/commons';
import { isNil } from 'lodash';
import { Subscription } from 'rxjs';
import { FilterableDropDownComponent } from '../filterable-drop-down/filterable-drop-down.component';
import { OptionsPickerDialogComponent } from '../options-picker-dialog/options-picker-dialog.component';

@Component({
  selector: 'rsa-select-options-picker',
  exportAs: 'rsaSelectOptionsPicker',
  templateUrl: './select-options-picker.component.html',
  styleUrls: ['./select-options-picker.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
@AutoUnsubscribe()
export class SelectOptionsPickerComponent
  extends ControlValueAccessorWithValidator<any>
  implements OnInit, AfterViewInit, OnChanges {
  @Input() set compareWith(fn: (o1, o2) => {}) {
    this.compareWithFn = fn;
  }

  constructor(private matDialog: MatDialog, private fb: FormBuilder, @Self() @Optional() public control: NgControl) {
    super();
    // Setting CVA for this component and get access to outer NgControl
    if (this.control) {
      this.control.valueAccessor = this;
    }

    this.form = fb.group({});
    this.valueChangesSubscription = this.form.valueChanges.subscribe(data => {
      this.setValueAndTriggerOnChanges(data[this.controlName]);
    });
  }

  public get invalid(): boolean {
    return this.control ? this.control.invalid : false;
  }
  @Input() selectId: string;
  @Input() selectClasses: string;
  @Input() labelTitle: string;
  @Input() labelClasses: string;
  @Input() placeholder: string;
  @Input() iconName: string;
  @Input() target: any;
  @Input() dialogTitle: string;
  @Input() dialogConfig: object;
  @Input() customErrors: object = {};
  @Input() pageable: Pageable;
  @Input() disabled = false;
  @Input() validateOn: string;
  @Input() clickDisabled = false;

  @Output() pageableChange = new EventEmitter<Pageable>();
  @Output() selectChange = new EventEmitter<any>();

  // Dynamic Data to Display
  @Input() options: any;
  @Input() optionsLabel: string;

  @ViewChild('select', { static: false }) select;

  controlName: string;

  readonly maxButtonsOnModalPicker = 8;
  confirmationDialog: MatDialogRef<FilterableDropDownComponent | OptionsPickerDialogComponent, any>;
  private pageableChangeSub: Subscription;

  writeValue(value: any) {
    super.writeValue(value);
    if (isNil(value)) {
      setTimeout(() => this.form.reset({ [this.controlName]: null }));
    } else {
      setTimeout(() => this.form.setValue({ [this.controlName]: value }));
    }
  }
  // Default object equality function
  compareWithFn: (o1, o2) => {} = (o1, o2) => o1 === o2;

  ngOnChanges(changes: SimpleChanges): void {
    if (
      changes &&
      changes.options &&
      changes.options.currentValue &&
      !changes.options.firstChange &&
      this.confirmationDialog &&
      this.confirmationDialog.componentInstance instanceof FilterableDropDownComponent
    ) {
      this.confirmationDialog.componentInstance.updateOptionList(changes.options.currentValue);
    }
    if (
      changes &&
      changes.options &&
      changes.disabled &&
      this.options?.length > 0 &&
      changes.disabled.currentValue !== changes.disabled.previousValue
    ) {
      const control = this.form.get(this.controlName);
      if (!this.disabled) {
        control?.enable();
      } else {
        control?.disable();
      }
    }
  }

  ngOnInit() {
    this.controlName = this.control && this.control.name ? this.control.name.toString() : 'options';
    this.form.addControl(this.controlName, new FormControl({ value: null, disabled: this.disabled }));
  }

  ngAfterViewInit(): void {
    if (this.control && this.control.control && this.control.control.validator) {
      this.form.get(this.controlName).setValidators(this.control.control.validator);
    }
  }

  /**
   * This should open a dialog with multiple options to select or filter. It will also fetch the selection object for the selected option
   * @param options is an object of buttons
   * @param optionsLabel the label that will be displayed in the list
   */
  openDialog(options: any, optionsLabel: string): void {
    const defaults = {
      height: 'auto',
      data: {
        options,
        optionsLabel,
        dialogTitle: this.dialogTitle,
        iconName: this.iconName,
        pageable: this.pageable,
      },
    };
    const modal = this.options
      ? this.options.length > this.maxButtonsOnModalPicker
        ? FilterableDropDownComponent
        : OptionsPickerDialogComponent
      : '';
    if (this.target || modal) {
      this.confirmationDialog = this.matDialog.open(this.target || modal, {
        ...defaults,
        ...(this.dialogConfig || {}),
      });

      //This is callback to fetch the selected option and update the field value
      this.confirmationDialog.afterClosed().subscribe(selectedOption => {
        if (selectedOption || (!selectedOption && !this.form.get(this.controlName).value)) {
          // this.form.setValue({options: selectedOption ? selectedOption : ''});
          const value = selectedOption ? selectedOption : '';
          this.form.get(this.controlName).setValue(value);
          this.selectChange.emit(value);
          this.pageable = undefined;
        }
      });
      if (this.confirmationDialog.componentInstance instanceof FilterableDropDownComponent) {
        this.pageableChangeSub = (this.confirmationDialog
          .componentInstance as FilterableDropDownComponent).pageableChange.subscribe(this.pageableChange);
      }
    }
  }

  selectOptions() {
    this.openDialog(this.options, this.optionsLabel);
  }

  selectClick() {
    if (this.clickDisabled) {
      return;
    }
    if (!this.form.disabled) {
      this.select.close();
      this.selectOptions();
    }
  }

  resetValue(value: any) {
    this.form.get(this.controlName).setValue(value);
    this.selectChange.emit(value);
  }
}
