import { Directive, Input } from '@angular/core';
import { FormControl, NG_VALIDATORS, ValidationErrors, Validator } from '@angular/forms';

@Directive({
  selector: '[rsaMinValueValidator]',
  providers: [{ provide: NG_VALIDATORS, useExisting: MinValueValidatorDirective, multi: true }],
})
export class MinValueValidatorDirective implements Validator {
  @Input() rsaMinValueValidator: number;

  validate(c: FormControl): ValidationErrors {
    if (c.value && this.rsaMinValueValidator) {
      const n = Number(c.value);
      if (n < this.rsaMinValueValidator) {
        return {
          invalid: true,
        };
      }
    }

    return null;
  }
}
