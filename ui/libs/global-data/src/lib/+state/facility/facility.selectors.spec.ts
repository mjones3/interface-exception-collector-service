import { FacilityEntity } from './facility.models';
import { facilityAdapter, FacilityState, initialFacilityState } from './facility.reducer';
import * as FacilitySelectors from './facility.selectors';

describe('Facility Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getFacilityId = it => it['id'];
  const createFacilityEntity = (id: string, name = '') =>
    ({
      id,
      name: name || `name-${id}`,
    } as FacilityEntity);

  let state;

  beforeEach(() => {
    state = {
      facility: facilityAdapter.setAll(
        [createFacilityEntity('PRODUCT-AAA'), createFacilityEntity('PRODUCT-BBB'), createFacilityEntity('PRODUCT-CCC')],
        {
          ...initialFacilityState,
          selectedId: 'PRODUCT-BBB',
          error: ERROR_MSG,
          loaded: true,
        }
      ),
    };
  });

  describe('Facility Selectors', () => {
    it('getAllFacility() should return the list of Facility', () => {
      const results = FacilitySelectors.getAllFacility(state);
      const selId = getFacilityId(results[1]);

      expect(results.length).toBe(3);
      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getSelected() should return the selected Entity', () => {
      const result = FacilitySelectors.getSelectedFacility(state);
      const selId = getFacilityId(result);

      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getFacilityLoaded() should return the current \'loaded\' status', () => {
      const result = FacilitySelectors.getFacilityLoaded(state);

      expect(result).toBe(true);
    });

    it('getFacilityError() should return the current \'error\' state', () => {
      const result = FacilitySelectors.getFacilityError(state);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
