import { provideHttpClient, withInterceptors } from '@angular/common/http';
import {
    ENVIRONMENT_INITIALIZER,
    EnvironmentProviders,
    Provider,
    inject,
} from '@angular/core';
import { AuthService } from 'app/shared/services';

export const provideAuth = (): Array<Provider | EnvironmentProviders> => {
    return [
        provideHttpClient(withInterceptors([AuthInterceptor])),
        {
            provide: ENVIRONMENT_INITIALIZER,
            useValue: () => inject(AuthService),
            multi: true,
        },
    ];
};
