import { Injectable } from '@angular/core';

import { Action, select, Store } from '@ngrx/store';

import * as FacilityActions from './facility.actions';
import * as FacilityFeature from './facility.reducer';
import * as FacilitySelectors from './facility.selectors';

@Injectable()
export class FacilityFacade {
  /**
   * Combine pieces of state using createSelector,
   * and expose them as observables through the facade.
   */
  loaded$ = this.store.pipe(select(FacilitySelectors.getFacilityLoaded));
  allFacility$ = this.store.pipe(select(FacilitySelectors.getAllFacility));
  selectedFacility$ = this.store.pipe(select(FacilitySelectors.getSelectedFacility));

  constructor(private store: Store) {}

  /**
   * Use the initialization action to perform one
   * or more tasks in your Effects.
   */
  init() {
    this.store.dispatch(FacilityActions.initFacility());
  }
}
