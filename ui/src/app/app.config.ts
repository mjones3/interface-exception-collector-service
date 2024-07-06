import { DatePipe } from '@angular/common';
import { provideHttpClient } from '@angular/common/http';
import { ApplicationConfig, isDevMode } from '@angular/core';
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
import { provideTransloco } from '@ngneat/transloco';
import { provideEffects } from '@ngrx/effects';
import { provideStore } from '@ngrx/store';
import { appRoutes } from 'app/app.routes';
import { provideIcons } from 'app/core/icons/icons.provider';
import { provideToastr } from 'ngx-toastr';
import { provideCore } from './core/core.provider';
import { TranslocoHttpLoader } from './core/transloco/transloco.http-loader';
import { mockApiServices } from './mock-api';

// export function createTranslateLoader(http: HttpClient) {
//     return new TranslateHttpLoader(http, './i18n/', '.json');
// }

export const appConfig: ApplicationConfig = {
    providers: [
        provideAnimations(),
        provideHttpClient(),
        provideRouter(
            appRoutes,
            withPreloading(PreloadAllModules),
            withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })
        ),

        // initialize the store and effects
        provideStore(),
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

        // Transloco Config
        provideTransloco({
            config: {
                availableLangs: [
                    {
                        id: 'en',
                        label: 'English',
                    },
                ],
                defaultLang: 'en',
                fallbackLang: 'en',
                reRenderOnLangChange: true,
                prodMode: !isDevMode(),
            },
            loader: TranslocoHttpLoader,
        }),
        provideCore(),
        provideToastr(),

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
