import { ActivatedRouteSnapshot } from '@angular/router';
import {ConfigurationService} from "./services/configuration.service";
import {inject} from "@angular/core";
import {IrradiationService} from "./services/irradiation.service";
import {IrradiationResolveData} from "./models/model";
import {map} from "rxjs";

export const irradiationResolver = (route: ActivatedRouteSnapshot) => {
    const configurationService = inject(ConfigurationService);
    const irradiationService = inject(IrradiationService);
    const USE_CHECK_DIGIT = 'USE_CHECK_DIGIT';

    const keys = [
        USE_CHECK_DIGIT
    ];

    return irradiationService.readConfiguration(keys).pipe(
        map((response) => {
            const data = response.data.readConfiguration;

            const resolverData: IrradiationResolveData = {
                useCheckDigit:
                    data.readConfiguration.find((conf) => conf.key === USE_CHECK_DIGIT).value ===
                    'Y'
            };

            configurationService.update(resolverData);
            return resolverData;
        })
    );

};
