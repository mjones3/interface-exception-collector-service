import { AbstractControl, ValidationErrors } from '@angular/forms';

export class BioproValidators {
    static hasAtLeastOne(control: AbstractControl): ValidationErrors | null {
        const validation =
            control &&
            Object.keys(control.value).some((field) => {
                return (
                    null !== control.value[field] &&
                    control.value[field] !== '' &&
                    (!Array.isArray(control.value[field]) ||
                        (Array.isArray(control.value[field]) &&
                            control.value[field].length > 0))
                );
            });
        return validation ? null : { hasntAtLeastOne: true };
    }
}
