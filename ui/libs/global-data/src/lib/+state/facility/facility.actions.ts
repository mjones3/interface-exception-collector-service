import { createAction, props } from '@ngrx/store';
import { FacilityEntity } from './facility.models';

export const initFacility = createAction('[Facility Page] Init');

export const loadFacilitySuccess = createAction(
  '[Facility/API] Load Facility Success',
  props<{ facility: FacilityEntity[] }>()
);

export const loadFacilityFailure = createAction('[Facility/API] Load Facility Failure', props<{ error: any }>());
