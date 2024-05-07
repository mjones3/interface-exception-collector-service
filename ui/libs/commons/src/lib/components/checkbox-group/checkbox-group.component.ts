import { Component, forwardRef, Input, OnInit } from '@angular/core';
import { ControlValueAccessor, NG_VALUE_ACCESSOR } from '@angular/forms';

@Component({
  selector: 'rsa-checkbox-group',
  templateUrl: './checkbox-group.component.html',
  providers: [
    {
      provide: NG_VALUE_ACCESSOR,
      useExisting: forwardRef(() => CheckboxGroupComponent),
      multi: true,
    },
  ],
})
export class CheckboxGroupComponent implements OnInit, ControlValueAccessor {

  @Input() itemsList: any[];

  _model: any[];
  _onChange: (value: any) => void;

  constructor() {
  }

  ngOnInit(): void {
    this._model = [];
  }

  writeValue(obj: any): void {
    if (obj !== '') {
      this._model = obj;
    }
  }

  registerOnChange(fn: any): void {
    this._onChange = fn;
  }

  registerOnTouched(fn: any): void {
  }

  setDisabledState?(isDisabled: boolean): void {
  }

  setValue(value: any): void {
    if (!this.contains(value)) {
      this._model.push(value);
    } else {
      this.remove(value);
    }

    this._onChange(this._model);
  }

  contains(value: any): boolean {
    if (this._model && this._model.length > 0) {
      const index = this._model.findIndex(valueOnModel => {
        if (valueOnModel.id) {
          return value.id === valueOnModel.id;
        } else {
          return value === valueOnModel;
        }
      });
      return index !== -1;
    }
    return false;
  }

  remove(value: any): void {
    if (this._model && this._model.length > 0) {
      const index = this._model.findIndex(valueOnModel => value.id === valueOnModel.id);
      if (index !== -1) {
        this._model.splice(index, 1);
        this._onChange(this._model);
      }
    }
  }

}
