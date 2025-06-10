import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { BehaviorSubject, catchError, debounceTime, first, map, Observable, switchMap, tap } from 'rxjs';
import { ReceivingService } from '../service/receiving.service';
import { ToastrService } from 'ngx-toastr';
import { ApolloError } from '@apollo/client';
import handleApolloError from '../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../shared/utils/notification.handling';

export class DeviceIdValidator {

    public static using(toastrService: ToastrService, receivingService: ReceivingService, locationCode: string, debounceTimeMillis: number = 1000): AsyncValidatorFn {
        const subject = new BehaviorSubject<string>('');
        const output = subject.asObservable().pipe(
            // Waits debounce time to trigger service validation
            debounceTime(debounceTimeMillis),
            // Takes only the first emitted value
            first(),
            // Triggers service validation using user input
            switchMap((deviceId: string) =>
                receivingService
                    .validateDevice({
                        bloodCenterId: deviceId,
                        locationCode: locationCode
                    })
                    .pipe(catchError((error: ApolloError) => handleApolloError(toastrService, error)))
            ),
            tap(response => {
                const notifications = response.data
                    ?.validateDevice
                    ?.notifications
                    ?.filter(n => n.type !== 'SUCCESS');
                consumeUseCaseNotifications(toastrService, notifications);
            }),
            map(response => {
                const warning = response.data
                    ?.validateDevice
                    ?.notifications
                    ?.filter(n => n.type === 'WARN')?.[0];
                return warning ? { deviceValidation: warning?.message } as ValidationErrors : null;
            }),
        );

        return (control: AbstractControl<string, string>): Observable<ValidationErrors | null> => {
            const deviceId = control.value;
            subject.next(deviceId);
            return output;
        };
    }

}
