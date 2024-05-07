import { HttpClient, HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { APP_INITIALIZER, ModuleWithProviders, Provider, Type } from '@angular/core';
import { MatMomentDateModule } from '@angular/material-moment-adapter';
import { ErrorStateMatcher } from '@angular/material/core';
import { Store } from '@ngrx/store';
import { MissingTranslationHandler, TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';
import { DefaultErrorStateMatcher } from '../../shared/forms/default.error-matcher';
import {
  EnvironmentConfigService,
  FacilityService,
  I18nService,
  IconService,
  ProcessService,
  ToastrImplService,
} from '../../shared/services/index';
import { AuthService } from '../auth/auth.service';
import { CustomMissingTranslationHandlerService } from '../i18n/custom-missing-translation-handler.service';
import { TranslationLoaderService } from '../i18n/translation-loader.service';
import { LoaderInterceptorService } from '../loader/loader-interceptor.service';
import { TimeZoneInterceptor } from '../time-zone/time-zone.interceptor';
import {
  APP_CONFIG_TOKEN,
  APP_ICONS_TOKEN,
  defaultInitializerConfigFactory,
  ENVIRONMENT_TOKEN,
} from './default-startup.loader';

export const COMMON_PROVIDERS: Provider[] = [
  {
    provide: APP_INITIALIZER,
    useFactory: defaultInitializerConfigFactory,
    multi: true,
    deps: [
      IconService,
      AuthService,
      TranslateService,
      I18nService,
      TranslationLoaderService,
      ProcessService,
      HttpClient,
      EnvironmentConfigService,
      Store,
      FacilityService,
      ENVIRONMENT_TOKEN,
      APP_ICONS_TOKEN,
      APP_CONFIG_TOKEN,
    ],
  },
  { provide: HTTP_INTERCEPTORS, useClass: LoaderInterceptorService, multi: true },
  { provide: HTTP_INTERCEPTORS, useClass: TimeZoneInterceptor, multi: true },
  { provide: ToastrService, useClass: ToastrImplService },
  { provide: ErrorStateMatcher, useClass: DefaultErrorStateMatcher },
];

export const COMMON_IMPORTS: Array<Type<any> | ModuleWithProviders<{}> | any[]> = [
  // ngx-translate and the loader module
  HttpClientModule,
  MatMomentDateModule,
  // Specify your library as an import
  TranslateModule.forRoot({
    missingTranslationHandler: {
      provide: MissingTranslationHandler,
      useClass: CustomMissingTranslationHandlerService,
    },
    loader: {
      provide: TranslateLoader,
      useClass: TranslationLoaderService,
    },
  }),
];
