import { AbstractControl, ValidatorFn } from '@angular/forms';
import * as moment from 'moment';
// @dynamic
export class DateValidator {
  static dateInThePass(AC: AbstractControl) {
    if (AC && AC.value && moment().isAfter(AC.value)) {
      const re = moment().isBefore(AC.value);
      return { dateInPass: true };
    }
    return null;
  }

  static dateValidator(AC: AbstractControl) {
    if (AC && AC.value && !moment(AC.value, 'YYYY-MM-DD', true).isValid()) {
      return { invalidDate: true };
    }
    return null;
  }

  static isOutOfRange(maxValue?: Date, minValue?: Date, minFieldName?: string): ValidatorFn {
    return (AC: AbstractControl): { [key: string]: any } | null => {
      let invalid = this.dateValidator(AC)?.hasOwnProperty('invalidDate');

      if (minFieldName) minValue = AC.parent?.controls[minFieldName]?.value ?? new Date();

      if (AC && AC.value && !invalid) {
        if (
          (maxValue && moment(maxValue).isValid() && moment(AC.value).isAfter(maxValue, 'day')) ||
          (minValue && moment(minValue).isValid() && moment(AC.value).isBefore(minValue, 'day'))
        ) {
          invalid = true;
        }
      }

      return invalid ? { outOfDate: true } : null;
    };
  }

  static dateValidators(AC: AbstractControl) {
    if (AC.errors && AC.errors.matDatepickerParse) {
      const date: any = new Date(AC.errors.matDatepickerParse.text);
      return { invalidDate: date === 'Invalid Date' };
    }
  }
}
