import { Injectable } from '@angular/core';
import settings from '../../../../public/settings.json';
import { Environment } from '../models';

@Injectable({ providedIn: 'root' })
export class EnvironmentConfigService {
    private _env: Environment;

    constructor() {
        console.debug(settings);
        this.env = settings as Environment;
    }

    get env(): Environment {
        return this._env;
    }

    set env(value: Environment) {
        this._env = {
            ...this._env,
            ...value,
        };
    }
}
