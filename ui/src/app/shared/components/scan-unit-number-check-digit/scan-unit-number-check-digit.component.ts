import {
  Component,
  EventEmitter,
  Input,
  Output,
  ViewChild,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { RsaValidators } from 'app/shared/forms/rsa-validators';
import {
  ValidationPipe,
  ValidationType,
} from 'app/shared/pipes/validation.pipe';
import { InputKeyboardComponent } from '../input-keyboard/input-keyboard.component';

@Component({
    standalone: true,
    imports: [ReactiveFormsModule, InputKeyboardComponent, ValidationPipe],
    selector: 'rsa-scan-unit-number-check-digit',
    templateUrl: './scan-unit-number-check-digit.component.html',
})
export class ScanUnitNumberCheckDigitComponent {
    @Input() labelTitle = '';
    @Input() disableCheckDigit = false;
    @Output() validate: EventEmitter<object> = new EventEmitter<object>();
    @Input() showKeyboard = true;
    @Input() inputFocus = false;

    @ViewChild('inputUnitNumber') inputUnitNumber: InputKeyboardComponent;
    @ViewChild('inputCheckDigit') inputCheckDigit: InputKeyboardComponent;

    checkDigitValidators = [Validators.required, Validators.maxLength(1)];

    form: FormGroup;
    readonly validationType = ValidationType;

    constructor(protected fb: FormBuilder) {
        this.form = this.fb.group({
            unitNumber: [null, [RsaValidators.unitNumber, Validators.required]],
            checkDigit: [
                { value: null, disabled: true },
                [...this.checkDigitValidators],
            ],
        });
    }

    get controlCheckDigit() {
        return this.form.controls['checkDigit'];
    }

    get controlUnitNumber() {
        return this.form.controls['unitNumber'];
    }

    checkValues(event) {
        let unitNumber: string = this.controlUnitNumber.value ?? '';
        const scanner = unitNumber.startsWith('=');
        const checkDigit: string = this.controlCheckDigit.value ?? '';

        if (scanner) {
            this.controlCheckDigit.setValidators(null);
            this.controlCheckDigit.updateValueAndValidity();
        }
        const hasValue =
            !!unitNumber &&
            (scanner ||
                (!this.disableCheckDigit && !!checkDigit) ||
                this.disableCheckDigit);

        if (hasValue && this.form.valid && this.validate) {
            if (!this.disableCheckDigit) {
                this.controlCheckDigit.setValidators([
                    ...this.checkDigitValidators,
                ]);
                this.controlCheckDigit.updateValueAndValidity();
            }

            if (scanner) {
                unitNumber = unitNumber.substring(1, unitNumber.length - 2);
            }

            const obj = {
                unitNumber: unitNumber.toUpperCase(),
                checkDigit: checkDigit.toUpperCase(),
                scanner: scanner,
            };

            this.validate.emit(obj);
        }
    }

    reset() {
        this.controlCheckDigit.disable({ emitEvent: false, onlySelf: true });
        this.form.reset();
    }

    focusOnUnitNumber() {
        this.inputUnitNumber.focusOnInput();
    }

    focusOnCheckDigit() {
        this.inputCheckDigit.focusOnInput();
    }

    enabledCheckDigit(event: string) {
        const scanner = this.controlUnitNumber.value
            ? this.controlUnitNumber.value.startsWith('=')
            : false;
        const value = this.controlUnitNumber.value ?? '';

        if (!this.disableCheckDigit) {
            if (scanner) {
                this.controlCheckDigit.disable({
                    emitEvent: false,
                    onlySelf: true,
                });
                this.controlCheckDigit.updateValueAndValidity();
            } else {
                if (value.length === 13) {
                    this.controlUnitNumber.updateValueAndValidity();
                }

                if (
                    this.controlUnitNumber.valid &&
                    this.controlUnitNumber.value
                ) {
                    this.controlCheckDigit.enable({
                        emitEvent: false,
                        onlySelf: true,
                    });
                } else {
                    this.controlCheckDigit.disable({
                        emitEvent: false,
                        onlySelf: true,
                    });
                }
            }
        }
    }
}
