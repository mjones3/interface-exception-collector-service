import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { catchError, map, Observable, of } from 'rxjs';
import { ReceivingService } from '../service/receiving.service';
import { ToastrService } from 'ngx-toastr';
import { ApolloError } from '@apollo/client';
import handleApolloError from '../../../shared/utils/apollo-error-handling';

export class DeviceIdValidator {

    public static using(toastrService: ToastrService, receivingService: ReceivingService, locationCode: string): AsyncValidatorFn {
        return (control: AbstractControl<string, string>): Observable<ValidationErrors | null> => {
            if (!control.value) {
                return of(null);
            }

            return receivingService
                .validateDevice({
                    bloodCenterId: control.value,
                    locationCode: locationCode
                })
                .pipe(
                    catchError((error: ApolloError) => handleApolloError(toastrService, error)),
                    map(response => {
                        const warning = response.data
                            ?.validateDevice
                            ?.notifications
                            ?.filter(n => n.type === 'WARN')?.[0];
                        return warning ? { deviceIdValidation: warning?.message } as ValidationErrors : null;
                    }),
                );
        };
    }

}
