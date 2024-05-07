import { NgModule } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { EffectsModule } from '@ngrx/effects';
import { Store, StoreModule } from '@ngrx/store';

import { NxModule } from '@nrwl/angular';
import { readFirst } from '@nrwl/angular/testing';

import * as ConfigurationActions from './configuration.actions';

import { ConfigurationFacade } from './configuration.facade';
import { ConfigurationEntity } from './configuration.models';
import { ConfigState, configurationReducerFactory, CONFIGURATION_FEATURE_KEY } from './configuration.reducer';

interface TestSchema {
  configuration: ConfigState;
}

describe('ConfigurationFacade', () => {
  let facade: ConfigurationFacade;
  let store: Store<TestSchema>;
  const createConfigurationEntity = (id: string, name = '') =>
    ({
      id,
      name: name || `name-${id}`,
    } as ConfigurationEntity);

  beforeEach(() => {});

  describe('used in NgModule', () => {
    beforeEach(() => {
      @NgModule({
        imports: [
          StoreModule.forFeature(CONFIGURATION_FEATURE_KEY, configurationReducerFactory),
          EffectsModule.forFeature([]),
        ],
        providers: [ConfigurationFacade],
      })
      class CustomFeatureModule {}

      @NgModule({
        imports: [NxModule.forRoot(), StoreModule.forRoot({}), EffectsModule.forRoot([]), CustomFeatureModule],
      })
      class RootModule {}
      TestBed.configureTestingModule({ imports: [RootModule] });

      store = TestBed.inject(Store);
      facade = TestBed.inject(ConfigurationFacade);
    });

    /**
     * The initially generated facade::loadAll() returns empty array
     */
    it('loadAll() should return empty list with loaded == true', async done => {
      try {
        const list = await readFirst(facade.allConfiguration$);
        const isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        done();
      } catch (err) {
        done.fail(err);
      }
    });

    /**
     * Use `loadConfigurationSuccess` to manually update list
     */
    it('allConfiguration$ should return the loaded list; and loaded flag == true', async done => {
      try {
        let isLoaded = await readFirst(facade.loaded$);
        expect(isLoaded).toBe(false);

        store.dispatch(
          ConfigurationActions.loadConfigurationSuccess({
            configuration: createConfigurationEntity('AAA'),
          })
        );

        isLoaded = await readFirst(facade.loaded$);

        expect(isLoaded).toBe(true);

        done();
      } catch (err) {
        done.fail(err);
      }
    });
  });
});
