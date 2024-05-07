import { AbstractControl, ValidationErrors } from '@angular/forms';
import { commonRegex } from '@rsa/commons';

export class TwoDigitNumberValidator {
  static isPositiveNumericAndHasTwoDigits(control: AbstractControl) {
    const value = control.value as string;
    if (value?.length !== 2 || isNaN(Number(value))) {
      return { invalidTwoDigitNumber: true };
    } else if (Number(value) < 0) {
      return { invalidTwoDigitNumber: true };
    }
    return null;
  }
}
