import { CommonModule } from '@angular/common';
import { InjectionToken, NgModule, Optional, SkipSelf } from '@angular/core';
import { EffectsModule } from '@ngrx/effects';
import { StoreRouterConnectingModule } from '@ngrx/router-store';
import { StoreModule } from '@ngrx/store';
import { AuthEffects } from './+state/auth/auth.effects';
import * as fromAuth from './+state/auth/auth.reducer';
import * as fromConfiguration from './+state/configuration/configuration.reducer';
import { FacilityEffects } from './+state/facility/facility.effects';
import * as fromFacility from './+state/facility/facility.reducer';

export const ROOT_REDUCER = new InjectionToken<any>('Root Reducer');

@NgModule({
  imports: [
    CommonModule,
    StoreModule.forRoot(ROOT_REDUCER),
    EffectsModule.forRoot([AuthEffects, FacilityEffects]),
    StoreRouterConnectingModule.forRoot(),
  ],
  providers: [
    {
      provide: ROOT_REDUCER,
      useValue: {
        auth: fromAuth.authReducerFactory,
        facility: fromFacility.facilityReducerFactory,
        configuration: fromConfiguration.configurationReducerFactory,
      },
    },
  ],
})
export class GlobalDataModule {
  /**
   * Constructor
   *
   * @param parentModule
   */
  constructor(@Optional() @SkipSelf() parentModule?: GlobalDataModule) {
    // Do not allow multiple injections
    if (parentModule) {
      throw new Error('GlobalDataModule has already been loaded. Import this module in the AppModule only.');
    }
  }
}
