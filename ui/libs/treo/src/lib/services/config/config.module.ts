import { ModuleWithProviders, NgModule } from '@angular/core';
import { TREO_APP_CONFIG } from './config.constants';
import { TreoConfigService } from './config.service';

@NgModule()
export class TreoConfigModule
{
    /**
     * Constructor
     *
     * @param {TreoConfigService} _treoConfigService
     */
    constructor(
        private _treoConfigService: TreoConfigService
    )
    {
    }

    /**
     * forRoot method for setting user configuration
     *
     * @param config
     */
    static forRoot(config: any): ModuleWithProviders<TreoConfigModule>
    {
        return {
            ngModule : TreoConfigModule,
            providers: [
                {
                    provide : TREO_APP_CONFIG,
                    useValue: config
                }
            ]
        };
    }
}
