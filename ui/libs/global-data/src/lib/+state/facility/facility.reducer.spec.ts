import * as FacilityActions from './facility.actions';
import { FacilityEntity } from './facility.models';
import { facilityReducerFactory, FacilityState, initialFacilityState } from './facility.reducer';

describe('Facility Reducer', () => {
  const createFacilityEntity = (id: string, name = '') =>
    ({
      id,
      name: name || `name-${id}`,
    } as FacilityEntity);

  beforeEach(() => {});

  describe('valid Facility actions', () => {
    it('loadFacilitySuccess should return set the list of known Facility', () => {
      const facility = [createFacilityEntity('PRODUCT-AAA'), createFacilityEntity('PRODUCT-zzz')];
      const action = FacilityActions.loadFacilitySuccess({ facility });

      const result: FacilityState = facilityReducerFactory(initialFacilityState, action);

      expect(result.loaded).toBe(true);
      expect(result.ids.length).toBe(2);
    });
  });

  describe('unknown action', () => {
    it('should return the previous state', () => {
      const action = {} as any;

      const result = facilityReducerFactory(initialFacilityState, action);

      expect(result).toBe(initialFacilityState);
    });
  });
});
