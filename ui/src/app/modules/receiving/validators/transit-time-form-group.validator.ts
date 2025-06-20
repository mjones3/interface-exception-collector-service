import { AbstractControl, ValidationErrors } from '@angular/forms';
import { DateTime } from 'luxon';

export const INVALID_MODEL_VALIDATION = ({ transitTimeValidation: true } as ValidationErrors);

export interface TransitTimeValidationModel {
    startDate?: DateTime;
    startTime?: string;
    startZone?: string;
    endDate?: DateTime;
    endTime?: string;
    endZone?: string;
}

export function isFormGroupCompletelyFilled(value: TransitTimeValidationModel): boolean {
    return !!(
        value?.startDate &&
        value?.startTime &&
        value?.startZone &&
        value?.endDate &&
        value?.endTime &&
        value?.endZone
    );
}

export function isDateRangeValid(startDate: DateTime, endDate: DateTime): boolean {
    const startDateTime = DateTime.fromISO(startDate.toISODate());
    if (!startDateTime.isValid) {
        return false;
    }
    const endDateTime = DateTime.fromISO(endDate.toISODate());
    if (!endDateTime.isValid) {
        return false;
    }
    return endDateTime.diff(startDateTime).milliseconds >= 0;
}

export class TransitTimeFormGroupValidator {

    public static validator(control: AbstractControl<TransitTimeValidationModel>): ValidationErrors | null {
        if (!isFormGroupCompletelyFilled(control.value)) {
            return INVALID_MODEL_VALIDATION;
        }
        if (!isDateRangeValid(control.value.startDate, control.value.endDate)) {
            return INVALID_MODEL_VALIDATION;
        }
        return null;
    }

}
