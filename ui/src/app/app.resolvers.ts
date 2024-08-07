import { inject } from '@angular/core';
import { Store } from '@ngrx/store';
import { TranslateService } from '@ngx-translate/core';
import { forkJoin, map } from 'rxjs';
import { loadConfigurationSuccess } from './core/state/configuration/configuration.actions';
import {
    EnvironmentConfigService,
    FacilityService,
    MenuService,
    ProcessService,
} from './shared/services';

export const initialDataResolver = () => {
    const menuService = inject(MenuService);
    const processService = inject(ProcessService);
    const config = inject(EnvironmentConfigService);
    const translateService = inject(TranslateService);
    const facilityService = inject(FacilityService);
    const store = inject(Store);

    translateService.addLangs(['en']);
    translateService.setDefaultLang('en');
    translateService.use('en');

    // Fork join multiple API endpoint calls to wait all of them to finish
    return forkJoin({
        menu: menuService.get(),
        processConfig: processService.getProcessConfiguration(config.env.uuid),
        productVersion: processService.getProcessProductVersion(
            config.env.uuid
        ),
    }).pipe(
        map(({ menu, processConfig, productVersion }) => {
            config.env.properties =
                processConfig.body?.properties ?? new Map([]);
            config.env.productVersion = productVersion.body;

            // Load configuration and auth in the store
            store.dispatch(
                loadConfigurationSuccess({ configuration: processConfig.body })
            );
            // If there is the facility cookie sync cookie and service
            if (facilityService.checkFacilityCookie()) {
                facilityService.syncCookieAndService();
            }
        })
    );
};
