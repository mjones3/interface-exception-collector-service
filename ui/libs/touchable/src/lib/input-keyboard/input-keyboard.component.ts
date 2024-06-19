import {
  ChangeDetectorRef,
  Component,
  ElementRef,
  EventEmitter,
  HostBinding,
  Input,
  OnChanges,
  Optional,
  Output,
  Self,
  SimpleChanges,
  TemplateRef,
  ViewChild,
} from '@angular/core';
import { FormBuilder, NgControl } from '@angular/forms';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { Autocomplete, AutoUnsubscribe, ControlValueAccessorWithValidator, Option } from '@rsa/commons';
import { take } from 'rxjs/operators';
import { InputType } from '../../shared/models/input-type.enum';
import { KeyboardTypeEnum } from '../../shared/models/keyboard-type.enum';
import { OnScreenKeyboardComponent } from '../on-screen-keyboard/on-screen-keyboard.component';

@Component({
  selector: 'rsa-input-keyboard',
  exportAs: 'rsaInputKeyboard',
  templateUrl: './input-keyboard.component.html',
  styleUrls: ['./input-keyboard.component.scss'],
})
@AutoUnsubscribe()
export class InputKeyboardComponent extends ControlValueAccessorWithValidator<string> implements OnChanges {
  @Input() labelTitle: string;
  @Input() labelClasses: string;
  @Input() labelWidth: string;
  @Input() inputWidth = '';
  @Input() inputId: string;
  @Input() inputType: InputType = InputType.TEXT;
  @Input() upperCase = false;
  @Input() textareaRows = 5;
  @Input() keyboardType: KeyboardTypeEnum;
  @Input() placeholder: string;
  @Input() iconName: string;
  @Input() closeOnReturn = true;
  @Input() submitFromKeyboard = true;
  @Input() regex = '';
  @Input() customErrors = {};
  @Input() options: Option[];
  @Input() optionsLabel: string;
  @Input() inputTemplate: TemplateRef<ElementRef>;
  @Input() maxLength?: number;
  @Input() highlightField: boolean;
  @Input() allowAutocomplete = Autocomplete.OFF;
  @Input() tabindex = 0;
  @Input() isDecimal = false;
  @Input() showKeyboard = true;
  @Input() suffixTemplateRef: TemplateRef<ElementRef>;
  @Input() disabled: boolean;
  @Input() required = false;

  @Input()
  set inputFocus(focus: boolean) {
    setTimeout(() => {
      this._inputFocus = focus;
      if (!this.cd['destroyed']) {
        this.cd.detectChanges();
      }
    }, 200);
  }

  get inputFocus() {
    return this._inputFocus;
  }

  @Output() inputFocusChange = new EventEmitter<boolean>();
  @Output() inputDataChange = new EventEmitter<boolean>();

  @Output() optionSelected: EventEmitter<Option> = new EventEmitter<Option>();
  @Output() tabOrEnterPressed: EventEmitter<string> = new EventEmitter<string>();
  @Output() inputChange: EventEmitter<string> = new EventEmitter<string>();

  @Output() keyUp: EventEmitter<string> = new EventEmitter<string>();

  @HostBinding('class')
  isInvalid: boolean;

  dialogRef: MatDialogRef<OnScreenKeyboardComponent, string>;
  inputTypeEnum = InputType;
  _inputFocus = false;

  @ViewChild('inputField') inputField: ElementRef;

  focusOnInput() {
    this.required = true;
    this.inputFocus = true;
  }

  constructor(
    private matDialog: MatDialog,
    private fb: FormBuilder,
    private cd: ChangeDetectorRef,
    @Self() @Optional() private control: NgControl,
    private el: ElementRef
  ) {
    super();

    // Setting CVA for this component and get access to outer NgControl
    if (control) {
      control.valueAccessor = this;
    }
    this.form = this.fb.group({
      input: ['', []],
    });

    this.valueChangesSubscription = this.form.valueChanges.subscribe(data => {
      let normalizedValue = !!data.input ? data.input : '';
      if (this.upperCase) {
        normalizedValue = normalizedValue.toUpperCase();
      }

      this.inputChange.emit(normalizedValue);
      this.setValueAndTriggerOnChanges(normalizedValue);
    });
    // Adding blur event to the angular component html element
    el.nativeElement.addEventListener('blur', () => {});
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.disabled && changes.disabled.currentValue !== changes.disabled.previousValue) {
      if (changes.disabled.currentValue) {
        this.form.controls['input'].disable({ emitEvent: false, onlySelf: true });
      } else {
        this.form.controls['input'].enable({ emitEvent: false, onlySelf: true });
      }
    }
  }

  onKeyUp(event: KeyboardEvent): void {
    if (this.keyUp) {
      this.keyUp.emit(event.key);
    }
  }

  onTabEnterPressed(): void {
    this.tabOrEnterPressed.emit(this.form.get('input').value);
  }

  writeValue(value: string) {
    let normalizedValue = !!value ? value : '';
    if (this.upperCase) {
      normalizedValue = normalizedValue.toUpperCase();
    }

    super.writeValue(normalizedValue);
    if (value === null) {
      this.form.reset({ input: null }, { emitEvent: false });
    } else {
      this.form.setValue({ input: normalizedValue });
    }
  }

  displayOnScreenKeyboard(): void {
    this.dialogRef = this.matDialog.open(OnScreenKeyboardComponent);
    this.dialogRef.componentInstance.inputType = this.inputType;
    this.dialogRef.componentInstance.keyboardType = this.keyboardType;
    this.dialogRef.componentInstance.value = this.value;
    this.dialogRef.componentInstance.regex = this.regex;
    if (this.control?.control.validator) {
      this.dialogRef.componentInstance.validators = this.control.control.validator;
    }
    this.dialogRef.componentInstance.customErrors = this.customErrors;
    this.dialogRef.componentInstance.inputTemplate = this.inputTemplate;
    this.dialogRef.componentInstance.options = this.options;
    this.dialogRef.componentInstance.optionsLabel = this.optionsLabel;
    this.dialogRef.componentInstance.maxLength = this.maxLength;
    this.dialogRef.componentInstance.autocomplete = this.allowAutocomplete;
    this.dialogRef.componentInstance.isDecimal = this.isDecimal;
    this.dialogRef.componentInstance.upperCase = this.upperCase;

    // Handle Enter Key press
    this.dialogRef.componentInstance.returnPressed.pipe(take(1)).subscribe(() => {
      if (this.submitFromKeyboard || this.closeOnReturn) {
        this.dialogRef.close(this.dialogRef.componentInstance.value);
      }
    });

    // Handle Filter Option Selected from Keyboard
    this.dialogRef.componentInstance.optionSelected.pipe(take(1)).subscribe(value => {
      this.optionSelected.emit(value);
      this.dialogRef.close();
    });

    // Handle Closing Keyboard from Accept Btn
    this.dialogRef.afterClosed().subscribe(value => {
      if (!!value) {
        this.form.setValue({ input: value });
        this.onTabEnterPressed();
        this.onBlur();
      }
    });
  }

  onBlur() {
    (this.el.nativeElement as HTMLElement).dispatchEvent(new Event('blur'));
    this.onTouched();
    this.inputFocusChange.emit(false);
    this.inputDataChange.emit(true);
  }

  onEnter($event) {
    ($event.target as HTMLInputElement).blur();
    this.onTabEnterPressed();
  }
}
