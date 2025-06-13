import { Directive, HostListener } from '@angular/core';
import { NgControl } from '@angular/forms';

@Directive({
    selector: '[appUppercase]',
    standalone: true,
})
export class UppercaseDirective {
    constructor(private ngControl: NgControl) {}

    @HostListener('input', ['$event.target.value']) onInputChange(
        value: string
    ) {
        const upperCaseValue = value.toUpperCase();
        if (value !== upperCaseValue) {
            const control = this.ngControl?.control;
            control.setValue(upperCaseValue, {
                emitEvent: true,
            });
        }
    }
}
