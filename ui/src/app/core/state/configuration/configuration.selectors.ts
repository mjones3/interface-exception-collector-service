import { createFeatureSelector, createSelector } from '@ngrx/store';
import {
    ConfigState,
    configurationAdapter,
    ConfigurationPartialState,
    CONFIGURATION_FEATURE_KEY
} from './configuration.reducer';

// Lookup the 'Configuration' feature state managed by NgRx
export const getConfigurationState = createFeatureSelector<ConfigurationPartialState, ConfigState>(
    CONFIGURATION_FEATURE_KEY
);

const { selectAll, selectEntities } = configurationAdapter.getSelectors();

export const getConfigurationLoaded = createSelector(getConfigurationState, (state: ConfigState) => state.loaded);

export const getConfigurationError = createSelector(getConfigurationState, (state: ConfigState) => state.error);

export const getAllConfiguration = createSelector(getConfigurationState, (state: ConfigState) => selectAll(state));

export const getConfigurationEntities = createSelector(getConfigurationState, (state: ConfigState) =>
    selectEntities(state)
);

export const getSelectedConfigId = createSelector(getConfigurationState, (state: ConfigState) => state.selectedId);

export const getSelectedConfig = createSelector(
    getConfigurationEntities,
    getSelectedConfigId,
    (entities, selectedId) => selectedId && entities[selectedId]
);
