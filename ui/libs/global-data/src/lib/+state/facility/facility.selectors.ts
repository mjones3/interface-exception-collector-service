import { createFeatureSelector, createSelector } from '@ngrx/store';
import { facilityAdapter, FacilityPartialState, FacilityState, FACILITY_FEATURE_KEY } from './facility.reducer';

// Lookup the 'Facility' feature state managed by NgRx
export const getFacilityState = createFeatureSelector<FacilityPartialState, FacilityState>(FACILITY_FEATURE_KEY);

const { selectAll, selectEntities } = facilityAdapter.getSelectors();

export const getFacilityLoaded = createSelector(getFacilityState, (state: FacilityState) => state.loaded);

export const getFacilityError = createSelector(getFacilityState, (state: FacilityState) => state.error);

export const getAllFacility = createSelector(getFacilityState, (state: FacilityState) => selectAll(state));

export const getFacilityEntities = createSelector(getFacilityState, (state: FacilityState) => selectEntities(state));

export const getSelectedFacilityId = createSelector(getFacilityState, (state: FacilityState) => state.selectedId);

export const getSelectedFacility = createSelector(
  getFacilityEntities,
  getSelectedFacilityId,
  (entities, selectedId) => selectedId && entities[selectedId]
);
