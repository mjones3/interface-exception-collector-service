import { AbstractControl } from '@angular/forms';

export class WhiteSpaceValidator {
  static validate(AC: AbstractControl) {
    const isWhitespace = (AC.value || '').trim().length === 0;
    return !isWhitespace ? null : { whiteSpace: true };
  }
}