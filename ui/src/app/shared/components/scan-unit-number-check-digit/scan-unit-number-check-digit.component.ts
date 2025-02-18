import { CommonModule } from '@angular/common';
import {
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnChanges,
    Output,
    SimpleChanges,
    ViewChild,
} from '@angular/core';
import {
    AbstractControl,
    FormBuilder,
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    ValidationErrors,
    ValidatorFn,
    Validators,
} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { TranslateService } from '@ngx-translate/core';
import { RsaValidators } from 'app/shared/forms/rsa-validators';
import { extractUnitNumber } from 'app/shared/utils/utils';

export function checkDigitValidator(valid: boolean): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
        const value = control.value;
        if (!value) {
            return null;
        }
        return !valid ? { invalid: true } : null;
    };
}

@Component({
    standalone: true,
    imports: [ReactiveFormsModule, MatInputModule, FormsModule, CommonModule],
    providers: [TranslateService],
    selector: 'rsa-scan-unit-number-check-digit',
    templateUrl: './scan-unit-number-check-digit.component.html',
})
export class ScanUnitNumberCheckDigitComponent implements OnChanges {
    @Input() showCheckDigit = false;
    @Input() inputFocus = false;
    @ViewChild('inputUnitNumber') inputUnitNumber: ElementRef;
    @ViewChild('inputCheckDigit') inputCheckDigit: ElementRef;
    @Output() validate: EventEmitter<{
        unitNumber: string;
        checkDigit: string;
        scanner: boolean;
    }> = new EventEmitter<{
        unitNumber: string;
        checkDigit: string;
        scanner: boolean;
    }>();
    @Output() keyUp: EventEmitter<string> = new EventEmitter<string>();
    form: FormGroup;
    checkDigitInvalidMessage: string;

    constructor(protected fb: FormBuilder) {
        this.form = this.fb.group({
            unitNumber: [null, [RsaValidators.unitNumber, Validators.required]],
        });
        setTimeout(() => {
            this.focusOnUnitNumber();
        }, 0);
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.showCheckDigit?.currentValue) {
            this.form.addControl(
                'checkDigit',
                new FormControl({ value: '', disabled: false }, [
                    Validators.required,
                    Validators.maxLength(1),
                    checkDigitValidator(false),
                ])
            );
            this.controlCheckDigit?.updateValueAndValidity();
        } else {
            this.form.removeControl('checkDigit');
        }
    }

    get controlCheckDigit() {
        return this.form.controls['checkDigit'];
    }

    get isScanner() {
        return !!this.controlUnitNumber.value?.startsWith('=');
    }

    get checkDigitVisible(): boolean {
        return (
            this.showCheckDigit &&
            !this.isScanner &&
            this.controlUnitNumber.value
        );
    }

    get isCheckDigitInvalid() {
        return this.controlCheckDigit.errors?.invalid;
    }

    get controlUnitNumber() {
        return this.form.controls['unitNumber'];
    }

    checkValues(checkDigitChange: boolean) {
        const unitNumber: string = this.controlUnitNumber?.value ?? '';
        const checkDigit: string = this.controlCheckDigit?.value ?? '';
        if (!this.isScanner) {
            this.focusOnCheckDigit();
        }
        const obj = {
            unitNumber: this.form.get('unitNumber').valid
                ? extractUnitNumber(unitNumber).toUpperCase()
                : '',
            checkDigit: checkDigit.toUpperCase(),
            scanner: this.isScanner,
            checkDigitChange,
        };
        this.validate.emit(obj);
    }

    enableDisableCheckDigit() {
        if (this.showCheckDigit) {
            if (this.isScanner) {
                this.controlCheckDigit?.setValue(null);
                this.controlCheckDigit?.disable();
            } else {
                if (
                    this.controlUnitNumber.value &&
                    this.controlUnitNumber.valid
                ) {
                    this.controlCheckDigit?.markAsUntouched();
                    this.controlCheckDigit?.enable();
                } else {
                    this.controlCheckDigit?.setValue(null);
                    this.controlCheckDigit?.disable();
                }
            }
        }
        this.checkValues(false);
    }

    reset(): void {
        this.controlCheckDigit?.disable({ emitEvent: false, onlySelf: true });
        this.form.reset();
        this.focusOnUnitNumber();
    }

    focusOnUnitNumber() {
        this.inputUnitNumber.nativeElement.focus();
    }

    focusOnCheckDigit() {
        this.inputCheckDigit?.nativeElement.focus();
    }

    setValidatorsForCheckDigit(valid: boolean): void {
        const digit = this.form.get('checkDigit');
        digit?.setValidators([
            Validators.required,
            Validators.maxLength(1),
            checkDigitValidator(valid),
        ]);
        this.form.get('checkDigit').updateValueAndValidity();
    }

    toUpperCase() {
        const checkDigitControl = this.form.get('checkDigit');
        const checkDigitValue =
            typeof checkDigitControl.value === 'string'
                ? checkDigitControl.value.toUpperCase()
                : checkDigitControl.value;
        checkDigitControl.setValue(checkDigitValue);
    }
}
