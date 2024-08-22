import {
    HttpClient,
    provideHttpClient,
    withInterceptors,
} from '@angular/common/http';
import {
    APP_INITIALIZER,
    ENVIRONMENT_INITIALIZER,
    EnvironmentProviders,
    Provider,
    inject,
} from '@angular/core';
import { ErrorStateMatcher } from '@angular/material/core';
import { InMemoryCache } from '@apollo/client/cache';
import { onError } from '@apollo/client/link/error';
import { Environment } from '@shared';
import { APOLLO_NAMED_OPTIONS, Apollo, NamedOptions } from 'apollo-angular';
import { HttpLink } from 'apollo-angular/http';
import { DefaultErrorStateMatcher } from 'app/shared/forms/default.error-match';
import { AuthService, EnvironmentConfigService } from 'app/shared/services';
import { ToastrImplService } from 'app/shared/services/toastr-impl.service';
import { KeycloakConfig } from 'keycloak-js';
import { ToastrService } from 'ngx-toastr';
import { switchMap } from 'rxjs';
import { NavigationMockApi } from '../mock-api/common/navigation/api';
import { ProcessMockApi } from '../mock-api/common/process/api';
import { authInterceptor } from './interceptors/auth.interceptor';
import { loaderInterceptor } from './interceptors/loader.interceptor';
import { timezoneInterceptor } from './interceptors/time-zone.interceptor';

const provideApollo = (): Provider[] => [
    {
        provide: APOLLO_NAMED_OPTIONS,
        useFactory: (
            httpLink: HttpLink,
            environmentConfigService: EnvironmentConfigService
        ): NamedOptions =>
            [
                '/graphql', // Default GraphQL path
                '/order/graphql',
                '/shipping/graphql',
            ].reduce(
                (instances: NamedOptions, path: string) => ({
                    ...instances,
                    [path]: {
                        link: onError(({ graphQLErrors, networkError }) => {
                            if (graphQLErrors) {
                                graphQLErrors.map(
                                    ({
                                        message,
                                        locations,
                                        path,
                                        extensions,
                                    }) =>
                                        console.error(
                                            `[GraphQL error]: Message: ${message}, Location: ${locations}, Path: ${path}, Extensions: ${extensions}`
                                        )
                                );
                            }
                            if (networkError) {
                                console.error(
                                    `[Network error]: ${networkError}`
                                );
                            }
                        }).concat(
                            httpLink.create({
                                uri:
                                    environmentConfigService.env.serverApiURL +
                                    path,
                            })
                        ),
                        cache: new InMemoryCache({ addTypename: false }),
                    },
                }),
                {}
            ),
        deps: [HttpLink, EnvironmentConfigService],
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
            useFactory: (
                http: HttpClient,
                authService: AuthService,
                processMockApi: ProcessMockApi,
                navigationMockApi: NavigationMockApi,
                environmentConfigService: EnvironmentConfigService
            ) => {
                processMockApi.registerHandlers();
                navigationMockApi.registerHandlers();
                return () =>
                    http.get<Environment>('/settings.json').pipe(
                        switchMap((settings: Environment) => {
                            environmentConfigService.env = { ...settings };
                            return authService.init({
                                config: environmentConfigService.env as KeycloakConfig,
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
            deps: [
                HttpClient,
                AuthService,
                ProcessMockApi,
                NavigationMockApi,
                EnvironmentConfigService,
            ],
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
