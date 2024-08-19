import { DatePipe } from '@angular/common';
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, importProvidersFrom } from '@angular/core';
import { LuxonDateAdapter } from '@angular/material-luxon-adapter';
import { DateAdapter, MAT_DATE_FORMATS } from '@angular/material/core';
import { provideAnimations } from '@angular/platform-browser/animations';
import {
    PreloadAllModules,
    provideRouter,
    withInMemoryScrolling,
    withPreloading,
} from '@angular/router';
import { provideFuse } from '@fuse';
import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';
import { TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TranslateHttpLoader } from '@ngx-translate/http-loader';
import { appRoutes } from 'app/app.routes';
import { provideIcons } from 'app/core/icons/icons.provider';
import { provideToastr } from 'ngx-toastr';
import { provideCore } from './core/core.provider';
import {
    AUTH_FEATURE_KEY,
    authReducerFactory,
} from './core/state/auth/auth.reducer';
import {
    CONFIGURATION_FEATURE_KEY,
    configurationReducerFactory,
} from './core/state/configuration/configuration.reducer';
import { mockApiServices } from './mock-api';
import { toasterDefaultConfig } from './shared/components/toaster/toaster-default.config';

export function createTranslateLoader(http: HttpClient) {
    return new TranslateHttpLoader(http, './i18n/', '.json');
}

export const appConfig: ApplicationConfig = {
    providers: [
        provideAnimations(),
        provideHttpClient(),

        // Translate Loader
        importProvidersFrom(
            TranslateModule.forRoot({
                loader: {
                    provide: TranslateLoader,
                    useFactory: createTranslateLoader,
                    deps: [HttpClient],
                },
            })
        ),

        provideRouter(
            appRoutes,
            withPreloading(PreloadAllModules),
            withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })
        ),

        // initialize the store and effects
        provideStore({
            [AUTH_FEATURE_KEY]: authReducerFactory,
            [CONFIGURATION_FEATURE_KEY]: configurationReducerFactory,
        }),

        provideEffects(),

        // Material Date Adapter
        {
            provide: DateAdapter,
            useClass: LuxonDateAdapter,
        },
        {
            provide: MAT_DATE_FORMATS,
            useValue: {
                parse: {
                    dateInput: 'D',
                },
                display: {
                    dateInput: 'DDD',
                    monthYearLabel: 'LLL yyyy',
                    dateA11yLabel: 'DD',
                    monthYearA11yLabel: 'LLLL yyyy',
                },
            },
        },

        provideCore(),
        provideToastr(toasterDefaultConfig),

        // Pipes
        DatePipe,

        // Fuse
        provideIcons(),
        provideFuse({
            mockApi: {
                delay: 0,
                services: mockApiServices,
            },
            fuse: {
                layout: 'classic',
                scheme: 'light',
                screens: {
                    sm: '600px',
                    md: '960px',
                    lg: '1280px',
                    xl: '1440px',
                },
                theme: 'fuse-theme-light',
                themes: [
                    {
                        id: 'fuse-theme-light',
                        name: 'Default',
                    },
                ],
            },
        }),
    ],
};
