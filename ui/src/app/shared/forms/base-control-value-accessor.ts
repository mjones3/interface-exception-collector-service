import { ControlValueAccessor } from '@angular/forms';

export class BaseControlValueAccessor<T> implements ControlValueAccessor {
    public disabled = false;
    public value: T;

    /**
     * Call when value has changed programmatically
     */
    onChange: any = (_: T) => {
    };

    onTouched: any = (_?: any) => {
    };

    registerOnChange(fn: any): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: any): void {
        this.onTouched = fn;
    }

    public setDisabledState?(isDisabled: boolean): void {
        this.disabled = isDisabled;
    }

    /**
     * Model -> View changes
     */
    writeValue(value: T): void {
        this.value = value;
    }

}
