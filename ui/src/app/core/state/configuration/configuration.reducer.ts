import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Action, createReducer, on } from '@ngrx/store';

import * as ConfigurationActions from './configuration.actions';
import { ConfigurationEntity } from './configuration.models';

export const CONFIGURATION_FEATURE_KEY = 'configuration';

export interface ConfigState extends EntityState<ConfigurationEntity> {
    selectedId?: string | number; // which Configuration record has been selected
    loaded: boolean; // has the Configuration list been loaded
    error?: string | null; // last known error (if any)
}

export interface ConfigurationPartialState {
    readonly [CONFIGURATION_FEATURE_KEY]: ConfigState;
}

export const configurationAdapter: EntityAdapter<ConfigurationEntity> = createEntityAdapter<ConfigurationEntity>();

export const initialConfigState: ConfigState = configurationAdapter.getInitialState({
    // set initial required properties
    loaded: false
});

const configurationReducer = createReducer(
    initialConfigState,
    on(ConfigurationActions.initConfig, state => ({ ...state, loaded: false, error: null })),
    on(ConfigurationActions.loadConfigurationSuccess, (state, configuration) => ({
        ...state,
        ...configuration,
        loaded: true
    })),
    on(ConfigurationActions.loadConfigurationFailure, (state, { error }) => ({ ...state, error }))
);

export function configurationReducerFactory(state: ConfigState | undefined, action: Action) {
    return configurationReducer(state, action);
}
