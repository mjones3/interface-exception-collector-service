import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { BehaviorSubject, catchError, debounceTime, first, map, Observable, switchMap } from 'rxjs';
import { ReceivingService } from '../service/receiving.service';
import { ToastrService } from 'ngx-toastr';
import { ApolloError } from '@apollo/client';
import handleApolloError from '../../../shared/utils/apollo-error-handling';

export class DeviceValidator {

    public static using(toastrService: ToastrService, receivingService: ReceivingService, locationCode: string, debounceTimeMillis: number = 400): AsyncValidatorFn {
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
            map(response =>
                response.data?.validateDevice?.notifications
                    ?.filter(n => n.type === 'WARN')
                    ?.map(n => ({ deviceValidation: n?.message } satisfies ValidationErrors))
                    ?.[0]
            )
        );

        return (control: AbstractControl<string, string>): Observable<ValidationErrors | null> => {
            const username = control.value;
            subject.next(username);
            return output;
        };
    }

}
