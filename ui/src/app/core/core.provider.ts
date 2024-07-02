import { HTTP_INTERCEPTORS, HttpClient } from '@angular/common/http';
import { APP_INITIALIZER, Provider } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { InMemoryCache } from '@apollo/client/cache';
import { ApolloClientOptions } from '@apollo/client/core';
import { onError } from '@apollo/client/link/error';
import { TranslocoService } from '@ngneat/transloco';
import { Store } from '@ngrx/store';
import { APOLLO_OPTIONS, Apollo } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { DefaultErrorStateMatcher } from 'app/shared/forms/default.error-match';
import { AUTH_SERVICE_TOKEN, AuthService, EnvironmentConfigService, FacilityService } from 'app/shared/services';
import { ProcessService } from 'app/shared/services/process.service';
import { ToastrImplService } from 'app/shared/services/toastr-impl.service';
import { environment } from 'environments/environment';
import { ToastrService } from 'ngx-toastr';
import { LoaderInterceptorService } from './interceptors/loader.interceptor';
import { TimeZoneInterceptor } from './interceptors/time-zone.interceptor';
import {
    ENVIRONMENT_TOKEN,
    defaultInitializerConfigFactory
} from './startup/default-startup.loader';

const provideApollo = (): Provider[] => {
    return [
        {
            provide: APOLLO_OPTIONS,
            useFactory: (httpLink: HttpLink): ApolloClientOptions<unknown> => {
                const http = httpLink.create({ uri: '/graphql' });
                const error = onError(({ graphQLErrors, networkError }) => {
                    if (graphQLErrors)
                        graphQLErrors.map(
                            ({ message, locations, path, extensions }) =>
                                console.error(
                                    `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}, Extensions: ${extensions}`
                                )
                        );

                    if (networkError)
                        console.error(`[Network error]: ${networkError}`);
                });
                const link = error.concat(http);

                return {
                    link,
                    cache: new InMemoryCache({
                        addTypename: false,
                    }),
                };
            },
            deps: [HttpLink],
        },
        Apollo,
    ];
};

const provideCommon = (): Provider[] => [
    {
        provide: APP_INITIALIZER,
        useFactory: defaultInitializerConfigFactory,
        deps: [
            AuthService,
            TranslocoService,
            ProcessService,
            HttpClient,
            EnvironmentConfigService,
            Store,
            FacilityService,
            ENVIRONMENT_TOKEN,
        ],
        multi: true,
    },
    {
        provide: HTTP_INTERCEPTORS,
        useClass: LoaderInterceptorService,
        multi: true,
    },
    { provide: HTTP_INTERCEPTORS, useClass: TimeZoneInterceptor, multi: true },
];

export const provideCore = (): Provider[] => {
    return [
        { provide: ENVIRONMENT_TOKEN, useValue: environment },
        { provide: AUTH_SERVICE_TOKEN, useExisting: AuthService },
        ...provideCommon(),
        { provide: ToastrService, useClass: ToastrImplService },
        { provide: ErrorStateMatcher, useClass: DefaultErrorStateMatcher },
        ...provideApollo(),
    ];
};
