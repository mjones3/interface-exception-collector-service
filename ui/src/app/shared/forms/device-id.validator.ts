import { AbstractControl, AsyncValidatorFn, ValidationErrors } from '@angular/forms';
import { catchError, first, map, Observable, of } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { ApolloError } from '@apollo/client';
import handleApolloError from '../utils/apollo-error-handling';
import { TemperatureDeviceService } from '../services/temperature-device.service';


export class DeviceIdValidator {

    public static asyncValidatorUsing(toastrService: ToastrService, temperatureDeviceService: TemperatureDeviceService, locationCode: string): AsyncValidatorFn {
        return (control: AbstractControl<string>): Observable<ValidationErrors | null> => {
            if (!control.value) {
                return of(null);
            }

            return temperatureDeviceService
                .validateDevice({
                    bloodCenterId: control.value,
                    locationCode: locationCode
                })
                .pipe(
                    first(),
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
