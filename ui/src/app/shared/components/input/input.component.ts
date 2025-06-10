import { NgClass } from '@angular/common';
import { AfterViewInit, booleanAttribute, ChangeDetectorRef, Component, ElementRef, EventEmitter, Input, numberAttribute, Optional, Output, Self } from '@angular/core';
import { FormGroup, FormBuilder, NgControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AutoFocusIfDirective } from 'app/shared/directive/auto-focus-if/auto-focus-if.directive';
import { MaskRegexDirective } from 'app/shared/directive/mask-regex/mask-regex.directive';
import { ControlValueAccessorWithValidator } from 'app/shared/forms/base-control-value-accessor-with-validator';
import { Autocomplete } from 'app/shared/types/autocomplete.enum';
import { InputType } from 'app/shared/types/input-type.enum';
import { Subscription } from 'rxjs';

@Component({
  selector: 'biopro-input',
  standalone: true,
  imports: [
    NgClass,
    ReactiveFormsModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    AutoFocusIfDirective,
    MaskRegexDirective
  ],
  templateUrl: './input.component.html'
})
export class InputComponent
extends ControlValueAccessorWithValidator<string>
implements AfterViewInit {
  readonly inputTypeEnum = InputType;

  @Input() label = '';
  @Input({ required: true }) inputId = '';
  @Input() inputType: InputType = InputType.TEXT;
  @Input({ transform: booleanAttribute }) upperCase = false; 
  @Input({ transform: numberAttribute }) textareaRows = 5;
  @Input() placeholder = '';
  @Input() regex = '';
  @Input() customErrors = {};
  @Input({ transform: numberAttribute }) maxLength?: number;
  @Input() allowAutocomplete = Autocomplete.OFF;
  @Input({ transform: numberAttribute }) tabindex = 0;
  @Input({ transform: booleanAttribute }) required = false;
  @Input() propagateParentErrors = false;
  @Input() hint?: string;
  @Input() suffix?: string;
  @Input() readOnly?: boolean = false;
  @Input() inputWidth: string;
  @Output() keyUp: EventEmitter<string> = new EventEmitter<string>();
  @Output() inputBlur: EventEmitter<string> = new EventEmitter<string>();

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

  @Output() tabOrEnterPressed: EventEmitter<string> = new EventEmitter<string>();
  @Output() inputChange: EventEmitter<string> = new EventEmitter<string>();

  form: FormGroup;

  parentChangeStatus: Subscription;

  private _inputFocus = false;

  constructor(
      private fb: FormBuilder,
      private cd: ChangeDetectorRef,
      @Self() @Optional() private ngControl: NgControl,
      private el: ElementRef
  ) {
      super();

      // Setting CVA for this component and get access to outer NgControl
      if (ngControl) {
          ngControl.valueAccessor = this;
      }

      this.form = fb.group({
          input: [''],
      });

      this.valueChangesSubscription = this.form.valueChanges.subscribe(
          (data) => {
              const normalizedValue = this.normalizeValue(data.input);
              this.inputChange.emit(normalizedValue);
              this.setValueAndTriggerOnChanges(normalizedValue);
          }
      );
      // Adding blur event to the angular component html element
      //el.nativeElement.addEventListener('blur', () => { });
  }

  ngAfterViewInit(): void {
      if (this.ngControl?.control?.validator) {
          this.form
              .get('input')
              .setValidators(this.ngControl.control.validator);
          this.cd.detectChanges();
      }

      if (this.propagateParentErrors) {
          this.parentChangeStatus = this.ngControl?.statusChanges.subscribe(
              (status) => {
                  const error =
                      status === 'INVALID' ? this.ngControl?.errors : null;
                  this.form.get('input').setErrors(error);
              }
          );
      }
  }

  onTabEnterPressed(event): void {
      event.preventDefault();
      this.tabOrEnterPressed.emit(this.form.get('input').value);
  }

  fireOnInputBlur(): void {
      this.inputBlur.emit(this.form.get('input').value);
  }

  handleKeyUp(event: KeyboardEvent): void {
      if (this.keyUp) {
          this.keyUp.emit(event.key);
      }
  }

  writeValue(value: string) {
      const normalizedValue = this.normalizeValue(value);
      super.writeValue(normalizedValue);
      if (value === null) {
          this.form.reset({ input: '' }, { emitEvent: false });
      } else {
          this.form.setValue({ input: normalizedValue });
      }
  }


  onBlur() {
      (this.el.nativeElement as HTMLElement).dispatchEvent(new Event('blur'));
      this.onTouched();
  }

  onEnter($event) {
      ($event.target as HTMLInputElement).blur();
      this.onTabEnterPressed($event);
  }

  normalizeValue(value: string) {
      let normalizedValue = value ?? '';
      if (this.upperCase) {
          normalizedValue = normalizedValue.toUpperCase();
      }
      return normalizedValue;
  }

  errorMessage() {
      return this.customErrors[Object.keys(this.form.get('input').errors)[0]];
  }
}
