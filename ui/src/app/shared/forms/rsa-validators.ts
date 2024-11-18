import {
    AbstractControl,
    FormGroup,
    ValidationErrors,
    ValidatorFn,
} from '@angular/forms';
import { commonRegex } from '../utils/utils';

export class RsaValidators {
    /**
     * @description
     * Validator that requires the control value length must be at least 13 characters and start with 'W'.
     *
     * @usageNotes
     *
     * ```typescript
     * const control = new FormControl('Q21312313213', RsaValidators.unitNumber);
     * console.log(control.errors); // {invalidUnitNumber: true}
     * ```
     * @returns An error map with the `invalidUnitNumber` property
     * if the validation check fails, otherwise `null`.
     *
     * @see `updateValueAndValidity()`
     */
    static unitNumber(control: AbstractControl): ValidationErrors | null {
        const value = control.value as string;

        // TODO check this validator rules
        if (
            value &&
            !new RegExp(commonRegex.unitNumber).test(value) &&
            !new RegExp(commonRegex.unitNumberWithZerosTail).test(value)
        ) {
            return { invalidUnitNumber: true };
        }
        return null;
    }

    static matchNumber(matchValue: string): ValidationErrors | null {
        const validation = (formGroup: FormGroup): ValidationErrors | null => {
            const currentValue = formGroup.value;
            if (currentValue && matchValue && currentValue !== matchValue) {
                return { invalidUnitNumber: true };
            }
            return null;
        };
        // TODO check this validator rules
        return validation;
    }

    static matchStringFn(matchValue: string): ValidatorFn {
        const validation = (formGroup: FormGroup): ValidationErrors | null => {
            const currentValue = formGroup.value;
            if (
                currentValue &&
                matchValue &&
                currentValue.toLowerCase() !== matchValue.toLowerCase()
            ) {
                return { notMatch: true };
            }
            return null;
        };
        // TODO check this validator rules
        return validation;
    }

    static matchStringArrFn(inputsArr: any, matchValue: string): ValidatorFn {
        const validation = (formGroup: FormGroup): ValidationErrors | null => {
            let returnValue = false;
            inputsArr.forEach((currentValue) => {
                if (
                    currentValue.value &&
                    matchValue &&
                    currentValue.value !== matchValue
                ) {
                    returnValue = true;
                }
            });
            return returnValue ? { notMatch: true } : null;
        };
        return validation;
    }

    static requiredArrFn(inputsArr: any, matchValue: string): ValidatorFn {
        const validation = (formGroup: FormGroup): ValidationErrors | null => {
            let returnValue = false;
            inputsArr.forEach((currentValue) => {
                if (matchValue && currentValue.value === '') {
                    returnValue = true;
                }
            });
            return returnValue ? { required: true } : null;
        };
        return validation;
    }

    static matchSelectStringFn(
        valueTobeMatched: string,
        matchValue: string
    ): ValidatorFn {
        const validation = (formGroup: FormGroup): ValidationErrors | null => {
            const currentValue = formGroup.value;
            if (
                currentValue &&
                matchValue &&
                currentValue.selectionKey !== matchValue
            ) {
                return { notMatch: true };
            }
            return null;
        };
        // TODO check this validator rules
        return validation;
    }

    static timeValidator(
        dateControl: string,
        hoursControl: string,
        minutesControl: string
    ): ValidatorFn {
        const validation = (formGroup: FormGroup): ValidationErrors | null => {
            const todayDate = new Date();
            const date = formGroup.get(dateControl).value;
            const hours = +formGroup.get(hoursControl).value;
            const minutes = +formGroup.get(minutesControl).value;

            if (
                formGroup.get(hoursControl).valid &&
                formGroup.get(minutesControl).valid
            ) {
                return date &&
                    date.diff(todayDate, 'days') === 0 &&
                    (hours > todayDate.getHours() ||
                        (hours === todayDate.getHours() &&
                            minutes > todayDate.getMinutes()))
                    ? { invalidTime: true }
                    : null;
            } else {
                return null;
            }
        };
        return validation;
    }

    static fullProductCode(control: AbstractControl): ValidationErrors | null {
        const value = control.value as string;

        if (
            value &&
            !new RegExp(commonRegex.scannedProductCode).test(value) &&
            !new RegExp(commonRegex.fullProductCode).test(value)
        ) {
            return { invalidProductCode: true };
        }
        return null;
    }

    static hasAtLeastOne(control: AbstractControl): ValidationErrors | null {
        const validation =
            control &&
            Object.keys(control.value).some((field) => {
                return (
                    control.value[field] !== '' &&
                    (!Array.isArray(control.value[field]) ||
                        (Array.isArray(control.value[field]) &&
                            control.value[field].length > 0))
                );
            });
        return validation ? null : { hasntAtLeastOne: true };
    }

    static aboRh(control: AbstractControl): ValidationErrors | null {
        const value = control.value as string;
        if (value && !new RegExp(commonRegex.aboRh).test(value)) {
            return { invalidAboRh: true };
        }
        return null;
    }

    static manualEntryValidatorForUnitNumber(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (!control.value) {
                return null;
            }
            const valid = control.value && control.value.startsWith('=');
            if (valid) {
                const validUnitNumber = RsaValidators.unitNumber(control);
                return validUnitNumber;
            } else {
                return { manualEntryUnitNumber: { value: control.value } };
            }
        };
    }

    static manualEntryValidatorForProductCode(): ValidatorFn {
        return (control: AbstractControl): ValidationErrors | null => {
            if (!control.value) {
                return null;
            }
            const valid = control.value && control.value.startsWith('=<');
            if (valid) {
                const validProductCode = RsaValidators.fullProductCode(control);
                return validProductCode;
            } else {
                return { manualEntryProductCode: { value: control.value } };
            }
        };
    }
}
