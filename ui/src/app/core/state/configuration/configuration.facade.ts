import { Injectable } from '@angular/core';

import { select, Store } from '@ngrx/store';

import * as ConfigurationActions from './configuration.actions';
import * as ConfigurationSelectors from './configuration.selectors';

@Injectable()
export class ConfigurationFacade {
  /**
   * Combine pieces of state using createSelector,
   * and expose them as observables through the facade.
   */
  loaded$ = this.store.pipe(select(ConfigurationSelectors.getConfigurationLoaded));
  allConfiguration$ = this.store.pipe(select(ConfigurationSelectors.getAllConfiguration));
  selectedConfiguration$ = this.store.pipe(select(ConfigurationSelectors.getSelectedConfig));
  getConfigurationState$ = this.store.pipe(select(ConfigurationSelectors.getConfigurationState));

  constructor(private store: Store) {}

  /**
   * Use the initialization action to perform one
   * or more tasks in your Effects.
   */
  init() {
    this.store.dispatch(ConfigurationActions.initConfig());
  }
}
