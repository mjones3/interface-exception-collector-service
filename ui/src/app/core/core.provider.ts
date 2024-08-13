import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { APP_INITIALIZER, ENVIRONMENT_INITIALIZER, EnvironmentProviders, inject, Provider } from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { InMemoryCache } from '@apollo/client/cache';
import { onError } from '@apollo/client/link/error';
import { Apollo, APOLLO_NAMED_OPTIONS, NamedOptions } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { DefaultErrorStateMatcher } from 'app/shared/forms/default.error-match';
import { Environment } from 'app/shared/models';
import { AuthService, EnvironmentConfigService } from 'app/shared/services';
import { ToastrImplService } from 'app/shared/services/toastr-impl.service';
import { KeycloakConfig } from 'keycloak-js';
import { ToastrService } from 'ngx-toastr';
import { switchMap } from 'rxjs';
import { authInterceptor } from './interceptors/auth.interceptor';
import { loaderInterceptor } from './interceptors/loader.interceptor';
import { timezoneInterceptor } from './interceptors/time-zone.interceptor';
import { ProcessMockApi } from '../mock-api/common/process/api';
import { NavigationMockApi } from '../mock-api/common/navigation/api';

const apolloErrorHandler = onError(({ graphQLErrors, networkError }) => {
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

const provideApollo = (): Provider[] => [
    {
        provide: APOLLO_NAMED_OPTIONS,
        useFactory: (httpLink: HttpLink): NamedOptions => {
            return {
                default: {
                    link: httpLink.create({ uri: '/graphql' }).concat(apolloErrorHandler),
                    cache: new InMemoryCache({ addTypename: false }),
                },
                order: {
                    link: httpLink.create({ uri: '/order/graphql' }).concat(apolloErrorHandler),
                    cache: new InMemoryCache({ addTypename: false }),
                },
                shipping: {
                    link: httpLink.create({ uri: '/shipping/graphql' }).concat(apolloErrorHandler),
                    cache: new InMemoryCache({ addTypename: false }),
                }
            };
        },
        deps: [HttpLink],
    },
    Apollo,
];

const provideHttpInterceptors = (): EnvironmentProviders[] => [
    provideHttpClient(
        withInterceptors([
            authInterceptor,
            loaderInterceptor,
            timezoneInterceptor,
        ])
    ),
];

export const provideCore = (): (Provider | EnvironmentProviders)[] => {
    return [
        {
            provide: APP_INITIALIZER,
            useFactory: () => {
                const authService = inject(AuthService);
                const processMockApi = inject(ProcessMockApi);
                const navigationMockApi = inject(NavigationMockApi);
                const config = inject(EnvironmentConfigService);
                const http = inject(HttpClient);

                return () =>
                    http.get('/settings.json').pipe(
                        switchMap((settings) => {
                            const environment = settings as Environment;
                            config.env = { ...(environment) };
                            processMockApi.registerHandlers(environment);
                            navigationMockApi.registerHandlers(environment);
                            return authService.init({
                                config: config.env as KeycloakConfig,
                                initOptions: {
                                    onLoad: 'check-sso',
                                    silentCheckSsoRedirectUri:
                                        window.location.origin +
                                        '/assets/silent-check-sso.html',
                                },
                                bearerExcludedUrls: ['/assets', 'assets'],
                            });
                        })
                    );
            },
            deps: [AuthService, EnvironmentConfigService, HttpClient],
            multi: true,
        },
        {
            provide: ENVIRONMENT_INITIALIZER,
            useValue: () => inject(AuthService),
            multi: true,
        },
        { provide: ToastrService, useClass: ToastrImplService },
        { provide: ErrorStateMatcher, useClass: DefaultErrorStateMatcher },
        ...provideHttpInterceptors(),
        ...provideApollo(),
    ];
};
