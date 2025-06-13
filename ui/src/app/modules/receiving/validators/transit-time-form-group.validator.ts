import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { DateTime } from 'luxon';
import { catchError, first, map, Observable, of } from 'rxjs';
import { ApolloError } from '@apollo/client';
import handleApolloError from '../../../shared/utils/apollo-error-handling';
import { ToastrService } from 'ngx-toastr';
import { ReceivingService } from '../service/receiving.service';
import { WritableSignal } from '@angular/core';
import { UseCaseNotificationDTO } from '../../../shared/models/use-case-response.dto';
import { consumeUseCaseNotifications } from '../../../shared/utils/notification.handling';

export const INVALID_MODEL_VALIDATION = ({ transitTimeValidation: true } as ValidationErrors);

export interface TransitTimeValidationModel {
    startDate?: DateTime;
    startTime?: string;
    startZone?: string;
    endDate?: DateTime;
    endTime?: string;
    endZone?: string;
}

export function isFormGroupModelFilled(value: TransitTimeValidationModel): boolean {
    return !!(
        value?.startDate &&
        value?.startTime &&
        value?.startZone &&
        value?.endDate &&
        value?.endTime &&
        value?.endZone
    );
}

export function getDateWithParsedTime(date: DateTime, time: string): DateTime<boolean> {
    const [hours, minutes] = time.split(':');
    return DateTime.fromISO(date.toISODate()).set({ hour: +hours, minute: +minutes });
}

export function isDateAndTimeRangeValid(startDate: DateTime, endDate: DateTime): boolean {
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
        if (!isFormGroupModelFilled(control.value)) {
            return INVALID_MODEL_VALIDATION;
        }
        if (!isDateAndTimeRangeValid(control.value.startDate, control.value.endDate)) {
            return INVALID_MODEL_VALIDATION;
        }
        return null;
    }

    public static asyncValidatorUsing(toastrService: ToastrService, receivingService: ReceivingService, temperatureCategory: string, temperatureQuarantineNotificationSignal: WritableSignal<UseCaseNotificationDTO>, transitTimeHumanReadableSignal: WritableSignal<string>): AsyncValidatorFn {
        return (control: AbstractControl<TransitTimeValidationModel>): Observable<ValidationErrors | null> => {
            const invalidModelErrors = TransitTimeFormGroupValidator.validator(control);
            if (invalidModelErrors) {
                return of(invalidModelErrors);
            }

            return receivingService
                .validateTransitTime({
                    temperatureCategory: temperatureCategory,
                    startDateTime: getDateWithParsedTime(control.value.startDate, control.value.startTime).toISO({ includeOffset: false, suppressMilliseconds: true }) + 'Z',
                    startTimeZone: control.value.startZone,
                    endDateTime: getDateWithParsedTime(control.value.endDate, control.value.endTime).toISO({ includeOffset: false, suppressMilliseconds: true }) + 'Z',
                    endTimeZone: control.value.endZone,
                })
                .pipe(
                    first(),
                    catchError((error: ApolloError) => handleApolloError(toastrService, error)),
                    map(response => {
                        const quarantineNotification = response.data?.validateTransitTime?.notifications?.filter(n => n.type === 'CAUTION')?.[0];
                        temperatureQuarantineNotificationSignal.set(quarantineNotification);

                        const otherNotifications = response.data?.validateTransitTime?.notifications?.filter(n => n.type !== 'CAUTION');
                        consumeUseCaseNotifications(toastrService, otherNotifications);

                        transitTimeHumanReadableSignal.set(response.data?.validateTransitTime?.data?.resultDescription);
                        return null;
                    }),
                );
        }
    }

}
