import {Injectable} from '@angular/core';
import {Environment} from '../models';

@Injectable({providedIn: 'root'})
export class EnvironmentConfigService {

  private _env: Environment;

  get env(): Environment {
    return this._env;
  }

  set env(value: Environment) {
    this._env = {
      ...this._env,
      ...value
    };
  }

}
