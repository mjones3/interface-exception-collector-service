import { HttpClient } from '@angular/common/http';
import { InjectionToken } from '@angular/core';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { loadConfigurationSuccess } from '@rsa/global-data';
import { KeycloakConfig } from 'keycloak-js';
import { merge } from 'lodash-es';
import { Observable, throwError, zip } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { toasterDefaultConfig } from '../../components/toaster/toaster-default.config';
import { TranslationLoaderService } from '../../core/i18n/translation-loader.service';
import { Environment } from '../../shared/models';
import { IconDefinition } from '../../shared/models/icon-types.model';
import { ModuleConfig } from '../../shared/models/module-config';
import { EnvironmentConfigService } from '../../shared/services/environment-config.service';
import { FacilityService, I18nService, ProcessService } from '../../shared/services/index';
import { AuthService } from '../auth/auth.service';

/**
 * Environment Token
 */
export const ENVIRONMENT_TOKEN = new InjectionToken('ENVIRONMENT_TOKEN');

/**
 * App Icon Token
 */
export const APP_ICONS_TOKEN = new InjectionToken<IconDefinition[]>('APP_ICONS_TOKEN');

/**
 * App Configuration token
 */
export const APP_CONFIG_TOKEN = new InjectionToken('APP_CONFIG_TOKEN');

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

export function errorHandler(error: any): Observable<any> {
  return throwError(error);
}

export function iconLoader(iconService, icons, defaultSet = 'assets/icons/material-twotone.svg'): Promise<any> {
  return new Promise(resolve => {
    iconService.addIcon(...icons);
    // Register icon sets (TODO Refactor this (all size more that 4MB should be better separate all icons and import it when need it))
    iconService.addIconSet(defaultSet);
    resolve(true);
  });
}

export function defaultTranslationAndProductConfigLoader(
  i18nLoader: TranslationLoaderService,
  translate: TranslateService,
  i18n: I18nService,
  processService: ProcessService,
  config: EnvironmentConfigService,
  module: string,
  appConfig: ModuleConfig
): Promise<any> {
  return new Promise((resolve, reject) => {
    // Load i18n translations
    zip(
      i18nLoader.getCustomTranslation(i18n.defaultLang, 'common'),
      i18nLoader.getCustomTranslation(i18n.defaultLang, module),
      processService.getProcessConfiguration(appConfig.processUUID),
      processService.getProcessProductVersion(appConfig.processUUID)
    )
      .pipe(
        catchError(res => {
          console.warn(`StartupService.load: Network request failed`, res);
          resolve(null);
          return [];
        })
      )
      .subscribe(
        ([commonTranslations, moduleTranslations, processConfiguration, processProductVersion]) => {
          // Setup language
          const mergeTranslations = merge(commonTranslations, moduleTranslations);
          const lang = processConfiguration.body.properties['default-language'] || i18n.defaultLang;
          translate.setTranslation(lang, mergeTranslations);
          translate.setDefaultLang(i18n.defaultLang);
          i18n.use(lang);

          // Setting environment properties
          config.env.properties = processConfiguration.body.properties;
          appConfig.appTreoConfig.name = processConfiguration.body.descriptionKey;
          appConfig.appTreoConfig.description = processConfiguration.body.descriptionKey;

          // Setting Process Product Version
          config.env.productVersion = processProductVersion.body;

          // Setting Toaster Config
          toasterDefaultConfig.timeOut = config.env.properties['message-timeout']
            ? +config.env.properties['message-timeout']
            : toasterDefaultConfig.timeOut;
          resolve(processConfiguration.body);
        },
        error => reject(error)
      );
  });
}

export function defaultInitializerConfigFactory(
  icon,
  authService: AuthService,
  translate: TranslateService,
  i18nService: I18nService,
  i18nLoader: TranslationLoaderService,
  processService: ProcessService,
  http: HttpClient,
  config: EnvironmentConfigService,
  store: Store,
  facilityService: FacilityService,
  environment,
  icons,
  appConfig
) {
  return () =>
    new Promise((resolve, reject) => {
      const configDeps: Promise<any>[] = [];
      http
        .get('/assets/settings.json') // load settings
        .pipe(catchError(errorHandler))
        .toPromise()
        .then(data => {
          // All logic needed
          config.env = { ...environment, ...(data as Environment) };

          // Add additional loaders
          configDeps.push(defaultKeycloakLoader(authService, config));
          configDeps.push(iconLoader(icon, icons));

          // Return resolved loaders
          return Promise.all(configDeps);
        })
        .then(() =>
          Promise.all([
            defaultTranslationAndProductConfigLoader(
              i18nLoader,
              translate,
              i18nService,
              processService,
              config,
              environment.module,
              appConfig
            ),
          ])
        )
        .then(([defaultConfig]) => {
          // Load configuration and auth in the store
          store.dispatch(loadConfigurationSuccess(defaultConfig));
          // If there is the facility cookie sync cookie and service
          if (facilityService.checkFacilityCookie()) {
            facilityService.syncCookieAndService().then(() => resolve(true));
          } else {
            resolve(true);
          }
        })
        .catch(err => reject(err));
    });
}
