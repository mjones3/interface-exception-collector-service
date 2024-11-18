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

    static eitherOrderNumberOrDatesValidator(
        control: AbstractControl
    ): ValidationErrors | null {
        const orderNumber = control.value.orderNumber;
        const createDateFrom = control.value.createDateFrom;
        const createDateTo = control.value.createDateTo;

        if (orderNumber || (createDateFrom && createDateTo)) {
            if (createDateFrom) {
                const twoYearsAgo = new Date();
                const datFrom = new Date(createDateFrom);
                const datTo = new Date(createDateTo);
                twoYearsAgo.setFullYear(twoYearsAgo.getFullYear() - 2);
                if (datFrom < twoYearsAgo) {
                    return {
                        eitherOrderNumberOrDates: true,
                        dateRangeExceedsTwoYears: true,
                    };
                }
                if (isNaN(datFrom.getDate())) {
                    return {
                        eitherOrderNumberOrDates: true,
                        matStartDateInvalid: true,
                    };
                }
                if (isNaN(datTo.getDate())) {
                    return {
                        eitherOrderNumberOrDates: true,
                        matEndDateInvalid: true,
                    };
                }
                if (datTo < datFrom) {
                    return {
                        eitherOrderNumberOrDates: true,
                        matEndDateInvalid: true,
                    };
                }
            }
            return null; // Validation passes
        } else {
            return { eitherOrderNumberOrDates: true }; // Validation fails
        }
    }
}
