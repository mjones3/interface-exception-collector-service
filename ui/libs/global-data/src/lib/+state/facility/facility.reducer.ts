import { createEntityAdapter, EntityAdapter, EntityState } from '@ngrx/entity';
import { Action, createReducer, on } from '@ngrx/store';

import * as FacilityActions from './facility.actions';
import { FacilityEntity } from './facility.models';

export const FACILITY_FEATURE_KEY = 'facility';

export interface FacilityState extends EntityState<FacilityEntity> {
  selectedId?: string | number; // which Facility record has been selected
  loaded: boolean; // has the Facility list been loaded
  error?: string | null; // last known error (if any)
}

export interface FacilityPartialState {
  readonly [FACILITY_FEATURE_KEY]: FacilityState;
}

export const facilityAdapter: EntityAdapter<FacilityEntity> = createEntityAdapter<FacilityEntity>();

export const initialFacilityState: FacilityState = facilityAdapter.getInitialState({
  // set initial required properties
  loaded: false,
});

const facilityReducer = createReducer(
  initialFacilityState,
  on(FacilityActions.initFacility, state => ({ ...state, loaded: false, error: null })),
  on(FacilityActions.loadFacilitySuccess, (state, { facility }) =>
    facilityAdapter.setAll(facility, { ...state, loaded: true })
  ),
  on(FacilityActions.loadFacilityFailure, (state, { error }) => ({ ...state, error }))
);

export function facilityReducerFactory(state: FacilityState | undefined, action: Action) {
  return facilityReducer(state, action);
}
