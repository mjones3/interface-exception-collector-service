import { CommonModule } from '@angular/common';
import {
    Component,
    ElementRef,
    EventEmitter,
    Input,
    Output,
    ViewChild,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { TranslateService } from '@ngx-translate/core';
import { RsaValidators } from 'app/shared/forms/rsa-validators';

@Component({
    standalone: true,
    imports: [ReactiveFormsModule, MatInputModule, FormsModule, CommonModule],
    providers: [TranslateService],
    selector: 'rsa-scan-unit-number-check-digit',
    templateUrl: './scan-unit-number-check-digit.component.html',
})
export class ScanUnitNumberCheckDigitComponent {
    @Input() disableCheckDigit = false;
    @Output() validate: EventEmitter<object> = new EventEmitter<object>();
    @Input() showKeyboard = true;
    @Input() inputFocus = false;
    @ViewChild('inputUnitNumber') inputUnitNumber: ElementRef;
    @ViewChild('inputCheckDigit') inputCheckDigit: ElementRef;
    @Output() tabOrEnterPressed: EventEmitter<string> =
        new EventEmitter<string>();
    @Output() keyUp: EventEmitter<string> = new EventEmitter<string>();

    checkDigitValidators = [Validators.required, Validators.maxLength(1)];

    form: FormGroup;

    constructor(
        protected fb: FormBuilder,
        private el: ElementRef
    ) {
        this.form = this.fb.group({
            unitNumber: [null, [RsaValidators.unitNumber, Validators.required]],
            checkDigit: [
                { value: null, disabled: true },
                [...this.checkDigitValidators],
            ],
        });
        setTimeout(() => {
            this.focusOnUnitNumber();
        }, 0);
    }

    get controlCheckDigit() {
        return this.form.controls['checkDigit'];
    }

    get controlUnitNumber() {
        return this.form.controls['unitNumber'];
    }

    checkValues() {
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
                checkDigit: checkDigit?.toUpperCase(),
                scanner: scanner,
            };

            this.validate.emit(obj);
        }
    }

    reset() {
        this.controlCheckDigit.disable({ emitEvent: false, onlySelf: true });
        this.form.reset();
        this.focusOnUnitNumber();
    }

    focusOnUnitNumber() {
        this.inputUnitNumber.nativeElement.focus();
    }

    focusOnCheckDigit() {
        this.inputCheckDigit.nativeElement.focus();
    }
}
