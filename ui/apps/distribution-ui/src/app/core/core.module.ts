import { NgModule, Optional, SkipSelf } from '@angular/core';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import {
  APP_CONFIG_TOKEN,
  APP_ICONS_TOKEN,
  AuthService,
  AUTH_SERVICE_TOKEN,
  COMMON_IMPORTS,
  COMMON_PROVIDERS,
  ENVIRONMENT_TOKEN,
} from '@rsa/commons';
import { appConfig } from '@rsa/distribution/core/config/app.config';
import { GlobalDataModule } from '@rsa/global-data';
import { environment } from '../../environments/environment';
import { DRIP_ICONS, HEROIC_ICONS, RSA_ICONS } from '../../icons';

@NgModule({
  imports: [...COMMON_IMPORTS, !environment.production ? StoreDevtoolsModule.instrument() : [], GlobalDataModule],
  providers: [
    { provide: ENVIRONMENT_TOKEN, useValue: environment },
    { provide: APP_ICONS_TOKEN, useValue: [...RSA_ICONS, ...HEROIC_ICONS, ...DRIP_ICONS] },
    { provide: APP_CONFIG_TOKEN, useValue: appConfig },
    { provide: AUTH_SERVICE_TOKEN, useExisting: AuthService },
    ...COMMON_PROVIDERS,
  ],
})
export class CoreModule {
  /**
   * Constructor
   *
   * @param parentModule
   */
  constructor(@Optional() @SkipSelf() parentModule?: CoreModule) {
    // Do not allow multiple injections
    if (parentModule) {
      throw new Error('CoreModule has already been loaded. Import this module in the AppModule only.');
    }
  }
}
