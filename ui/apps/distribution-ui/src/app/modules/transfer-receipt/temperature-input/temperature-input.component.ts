import { Component, EventEmitter, Input, Optional, Output, Self } from '@angular/core';
import { FormControl, NgControl } from '@angular/forms';
import { ValidationType } from '../../../../../../../libs/commons/src/lib/pipes/validation.pipe';
import { ControlValueAccessorWithValidator } from '../../../../../../../libs/commons/src/lib/shared/forms/base-control-value-accesor-with-validator';

export type TemperatureSignType = '+' | '-';
export type TemperatureUnitType = 'celsius' | 'fahrenheit';

@Component({
  selector: 'rsa-temperature-input',
  templateUrl: './temperature-input.component.html',
  styleUrls: ['./temperature-input.component.scss'],
})
export class TemperatureInputComponent extends ControlValueAccessorWithValidator<string> {
  @Input() inputWidth = 'flex-auto';

  @Output() temperatureSignChange = new EventEmitter();

  selectedSignOfTemperature: TemperatureSignType = '+';
  readonly validationType = ValidationType;
  temperatureControl = new FormControl('');
  abs = Math.abs;

  constructor(@Self() @Optional() private control: NgControl) {
    super();
    // Setting CVA for this component and get access to outer NgControl
    if (control) {
      control.valueAccessor = this;
    }
    this.valueChangesSubscription = this.temperatureControl.valueChanges.subscribe(data => {
      this.setValueAndTriggerOnChanges(this.selectedSignOfTemperature + data);
    });
  }

  writeValue(value: string) {
    const valueWithSign = value && ['+', '-'].includes(value?.charAt(0));
    this.onToggleTemperature(valueWithSign ? (value?.charAt(0) as TemperatureSignType) : '+');
    const newValue = valueWithSign ? value.slice(1) : value;
    super.writeValue(newValue);
    if (newValue === null) {
      this.temperatureControl.reset(null, { emitEvent: false });
    } else {
      this.temperatureControl.setValue(newValue);
    }
  }

  onToggleTemperature(s: TemperatureSignType) {
    this.selectedSignOfTemperature = s;
    this.temperatureSignChange.emit(s);
    const value = Number(this.temperatureControl.value) || 0;
    this.temperatureControl.setValue(value);
  }
}
