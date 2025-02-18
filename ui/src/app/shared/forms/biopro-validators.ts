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
        const createDateFrom = control.value.createDate?.start;
        const createDateTo = control.value.createDate?.end;

        if (orderNumber || (createDateFrom && createDateTo)) {
            if (createDateFrom || createDateTo) {
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                const twoYearsAgo = new Date();
                const datFrom = new Date(createDateFrom);
                const datTo = new Date(createDateTo);
                datFrom.setHours(0, 0, 0, 0);
                datTo.setHours(0, 0, 0, 0);
                twoYearsAgo.setHours(0, 0, 0, 0);
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
                        initialDateGreaterThanFinalDate: true,
                    };
                }
                if (datTo > today) {
                    return { matDatepickerMaxMessage: true };
                }
            }
            return null; // Validation passes
        } else {
            return { eitherOrderNumberOrDates: true }; // Validation fails
        }
    }
}
