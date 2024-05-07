import { NgModule } from '@angular/core';
import { TestBed } from '@angular/core/testing';

import { EffectsModule } from '@ngrx/effects';
import { Store, StoreModule } from '@ngrx/store';

import { NxModule } from '@nrwl/angular';
import { readFirst } from '@nrwl/angular/testing';

import * as FacilityActions from './facility.actions';

import { FacilityEffects } from './facility.effects';
import { FacilityFacade } from './facility.facade';
import { FacilityEntity } from './facility.models';
import { facilityReducerFactory, FacilityState, FACILITY_FEATURE_KEY } from './facility.reducer';

interface TestSchema {
  facility: FacilityState;
}

describe('FacilityFacade', () => {
  let facade: FacilityFacade;
  let store: Store<TestSchema>;
  const createFacilityEntity = (id: string, name = '') =>
    ({
      id,
      name: name || `name-${id}`,
    } as FacilityEntity);

  beforeEach(() => {});

  describe('used in NgModule', () => {
    beforeEach(() => {
      @NgModule({
        imports: [
          StoreModule.forFeature(FACILITY_FEATURE_KEY, facilityReducerFactory),
          EffectsModule.forFeature([FacilityEffects]),
        ],
        providers: [FacilityFacade],
      })
      class CustomFeatureModule {}

      @NgModule({
        imports: [NxModule.forRoot(), StoreModule.forRoot({}), EffectsModule.forRoot([]), CustomFeatureModule],
      })
      class RootModule {}
      TestBed.configureTestingModule({ imports: [RootModule] });

      store = TestBed.inject(Store);
      facade = TestBed.inject(FacilityFacade);
    });

    /**
     * The initially generated facade::loadAll() returns empty array
     */
    it('loadAll() should return empty list with loaded == true', async done => {
      try {
        let list = await readFirst(facade.allFacility$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        facade.init();

        list = await readFirst(facade.allFacility$);
        isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(true);

        done();
      } catch (err) {
        done.fail(err);
      }
    });

    /**
     * Use `loadFacilitySuccess` to manually update list
     */
    it('allFacility$ should return the loaded list; and loaded flag == true', async done => {
      try {
        let list = await readFirst(facade.allFacility$);
        let isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(0);
        expect(isLoaded).toBe(false);

        store.dispatch(
          FacilityActions.loadFacilitySuccess({
            facility: [createFacilityEntity('AAA'), createFacilityEntity('BBB')],
          })
        );

        list = await readFirst(facade.allFacility$);
        isLoaded = await readFirst(facade.loaded$);

        expect(list.length).toBe(2);
        expect(isLoaded).toBe(true);

        done();
      } catch (err) {
        done.fail(err);
      }
    });
  });
});
