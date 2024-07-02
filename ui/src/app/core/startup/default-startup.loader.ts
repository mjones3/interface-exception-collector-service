import { HttpClient } from '@angular/common/http';
import { InjectionToken } from '@angular/core';
import { TranslocoService } from '@ngneat/transloco';
import { Store } from '@ngrx/store';
import { toasterDefaultConfig } from 'app/shared/components/toaster/toaster-default.config';
import { Environment } from 'app/shared/models';
import { AuthService, EnvironmentConfigService, FacilityService } from 'app/shared/services';
import { ProcessService } from 'app/shared/services/process.service';
import { KeycloakConfig } from 'keycloak-js';
import { Observable, throwError, zip } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { loadConfigurationSuccess } from '../state/configuration/configuration.actions';


/**
 * Environment Token
 */
export const ENVIRONMENT_TOKEN = new InjectionToken('ENVIRONMENT_TOKEN');


export function errorHandler(error: any): Observable<any> {
    return throwError(error);
}

export function defaultKeycloakLoader(authService: AuthService, config: EnvironmentConfigService): Promise<boolean> {
    return authService.init({
      config: config.env as KeycloakConfig,
      initOptions: {
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/assets/silent-check-sso.html',
      },
      bearerExcludedUrls: ['/assets', 'assets'],
    });
  }

export function defaultTranslationAndProductConfigLoader(
    translocoService: TranslocoService,
    processService: ProcessService,
    config: EnvironmentConfigService,
): Promise<any> {
    return new Promise((resolve, reject) => {
        // Setup language
        const defaultLang = translocoService.getDefaultLang();
        translocoService.setActiveLang(defaultLang);

        zip(
            processService.getProcessConfiguration(config.env.uuid),
            processService.getProcessProductVersion(config.env.uuid),
            translocoService.load(defaultLang)
        )
            .pipe(
                catchError((res) => {
                    console.warn(
                        `StartupService.load: Network request failed`,
                        res
                    );
                    resolve(null);
                    return [];
                })
            )
            .subscribe({
                next: ([processConfiguration, processProductVersion]) => {
                    // Setting environment properties
                    config.env.properties =
                        processConfiguration.body.properties;

                    // Setting Process Product Version
                    config.env.productVersion = processProductVersion.body;

                    // Setting Toaster Config
                    toasterDefaultConfig.timeOut = config.env.properties[
                        'message-timeout'
                    ]
                        ? +config.env.properties['message-timeout']
                        : toasterDefaultConfig.timeOut;
                    resolve(processConfiguration.body);
                },
                error: (error) => reject(error),
            });
    });
}



export function defaultInitializerConfigFactory(
    authService: AuthService,
    translocoService: TranslocoService,
    processService: ProcessService,
    http: HttpClient,
    config: EnvironmentConfigService,
    store: Store,
    facilityService: FacilityService,
    environment,
) {
    return () =>
        new Promise((resolve, reject) => {
            const configDeps: Promise<any>[] = [];
            http.get('/settings.json') // load settings
                .pipe(catchError(errorHandler))
                .toPromise()
                .then((data) => {
                    // All logic needed
                    config.env = { ...environment, ...(data as Environment) };

                    // configDeps.push(defaultKeycloakLoader(authService, config));

                    // Return resolved loaders
                    return Promise.all(configDeps);
                })
                .then(() =>
                    Promise.all([
                        defaultTranslationAndProductConfigLoader(
                            translocoService,
                            processService,
                            config,
                        ),
                    ])
                )
                .then(([defaultConfig]) => {
                    // Load configuration and auth in the store
                    store.dispatch(loadConfigurationSuccess(defaultConfig));
                    // If there is the facility cookie sync cookie and service
                    if (facilityService.checkFacilityCookie()) {
                        facilityService
                            .syncCookieAndService()
                            .then(() => resolve(true));
                    } else {
                        resolve(true);
                    }
                })
                .catch((err) => reject(err));
        });
}


