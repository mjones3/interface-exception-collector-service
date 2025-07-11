import { Injectable, signal } from '@angular/core';
import {IrradiationResolveData} from "../models/model";

@Injectable({
    providedIn: 'root',
})
export class ConfigurationService {
    readonly configuration = signal<IrradiationResolveData>(null);

    update(configuration: IrradiationResolveData) {
        this.configuration.set(configuration);
    }
}
