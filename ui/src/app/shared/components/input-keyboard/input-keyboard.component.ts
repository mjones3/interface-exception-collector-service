import { CommonModule } from '@angular/common';
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
import { FormBuilder, NgControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { AutoUnsubscribe } from '../../../shared/decorators/auto-unsubscribe/auto-unsubscribe.decorator';
import { ControlErrorsDirective } from '../../../shared/directive/control-errors/control-errors.directive';
import { MaskRegexDirective } from '../../../shared/directive/mask-regex/mask-regex.directive';
import { ControlValueAccessorWithValidator } from '../../../shared/forms/base-control-value-accessor-with-validator';
import { Option } from '../../../shared/models';
import { Autocomplete } from '../../../shared/types/autocomplete.enum';
import { InputType } from '../../../shared/types/input-type.enum';
import { KeyboardTypeEnum } from '../../../shared/types/keyboard-type.enum';

@Component({
    selector: 'rsa-input-keyboard',
    templateUrl: './input-keyboard.component.html',
    styleUrls: ['./input-keyboard.component.scss'],
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        MatInputModule,
        ControlErrorsDirective,
        MaskRegexDirective
    ],
})
@AutoUnsubscribe()
export class InputKeyboardComponent
    extends ControlValueAccessorWithValidator<string>
    implements OnChanges
{
    @Input() labelTitle: '';
    @Input() labelClasses: '';
    @Input() labelWidth: '';
    @Input() inputWidth = '';
    @Input() inputId: '';
    @Input() inputType: InputType = InputType.TEXT;
    @Input() upperCase = false;
    @Input() textareaRows = 5;
    @Input() keyboardType: KeyboardTypeEnum;
    @Input() placeholder = '';
    @Input() iconName: '';
    @Input() closeOnReturn = true;
    @Input() submitFromKeyboard = true;
    @Input() regex = '';
    @Input() customErrors = {};
    @Input() options: Option[];
    @Input() optionsLabel: '';
    @Input() inputTemplate: TemplateRef<ElementRef>;
    @Input() maxLength?: number;
    @Input() highlightField: boolean;
    @Input() allowAutocomplete = Autocomplete.OFF;
    @Input() tabindex = 0;
    @Input() isDecimal = false;
    @Input() showKeyboard = true;
    @Input() suffixTemplateRef: TemplateRef<ElementRef>;
    @Input() disabled: boolean;

    @Output() inputFocusChange = new EventEmitter<boolean>();
    @Output() inputDataChange = new EventEmitter<boolean>();
    @Output() optionSelected = new EventEmitter<Option>();
    @Output() tabOrEnterPressed: EventEmitter<string> =
        new EventEmitter<string>();
    @Output() inputChange: EventEmitter<string> = new EventEmitter<string>();
    @Output() keyUp: EventEmitter<string> = new EventEmitter<string>();
    @HostBinding('class')
    isInvalid: boolean;
    inputTypeEnum = InputType;
    @Input() required: boolean = false;
    @ViewChild('inputField') inputField: ElementRef;

    constructor(
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

        this.valueChangesSubscription = this.form.valueChanges.subscribe(
            (data) => {
                let normalizedValue = data.input ?? '';
                if (this.upperCase) {
                    normalizedValue = normalizedValue.toUpperCase();
                }

                this.inputChange.emit(normalizedValue);
                this.setValueAndTriggerOnChanges(normalizedValue);
            }
        );
        // Adding blur event to the angular component html element
        el.nativeElement.addEventListener('blur', () => {});
    }

    _inputFocus = false;

    get inputFocus() {
        return this._inputFocus;
    }

    @Input()
    set inputFocus(focus: boolean) {
        setTimeout(() => {
            this._inputFocus = focus;
            if (!this.cd['destroyed']) {
                this.cd.detectChanges();
            }
        }, 200);
    }

    focusOnInput() {
        this.inputFocus = true;
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (
            changes.disabled &&
            changes.disabled.currentValue !== changes.disabled.previousValue
        ) {
            if (changes.disabled.currentValue) {
                this.form.controls['input'].disable({
                    emitEvent: false,
                    onlySelf: true,
                });
            } else {
                this.form.controls['input'].enable({
                    emitEvent: false,
                    onlySelf: true,
                });
            }
        }
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

    onKeyUp(event: KeyboardEvent): void {
        if (this.keyUp) {
            this.keyUp.emit(event.key);
        }
    }

    onTabEnterPressed(): void {
        this.tabOrEnterPressed.emit(this.form.get('input').value);
    }

    writeValue(value: string) {
        let normalizedValue = value ?? '';
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
}
