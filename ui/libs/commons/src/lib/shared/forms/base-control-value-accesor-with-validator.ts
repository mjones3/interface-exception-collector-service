import { FormGroup } from '@angular/forms';
import { Subscription } from 'rxjs';
import { BaseControlValueAccessor } from './base-control-value-accesor';

/**
 * Class that implement the common functionalities of Control Value Accessor and implement validator interface
 */
export class ControlValueAccessorWithValidator<T> extends BaseControlValueAccessor<T> {
  form: FormGroup;
  valueChangesSubscription: Subscription;

  constructor() {
    super();
  }

  setValueAndTriggerOnChanges(value: any) {
    this.value = value;
    this.onChange(value);
  }

  setDisabledState(isDisabled: boolean) {
    super.setDisabledState(isDisabled);
    setTimeout(() => {
      if (isDisabled) {
        this.form.disable();
      } else {
        this.form.enable({ emitEvent: false });
      }
    });
  }
}
