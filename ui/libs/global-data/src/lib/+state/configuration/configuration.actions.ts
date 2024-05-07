import { createAction, props } from '@ngrx/store';
import { ConfigurationEntity } from './configuration.models';

export const initConfig = createAction('[Configuration Page] Init');

export const loadConfigurationSuccess = createAction(
  '[Configuration/API] Load Configuration Success',
  props<{ configuration: ConfigurationEntity }>()
);

export const loadConfigurationFailure = createAction(
  '[Configuration/API] Load Configuration Failure',
  props<{ error: any }>()
);
