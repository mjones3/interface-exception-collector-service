import { ConfigurationEntity } from './configuration.models';
import { ConfigState, configurationAdapter, initialConfigState } from './configuration.reducer';
import * as ConfigurationSelectors from './configuration.selectors';

describe('Configuration Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getConfigurationId = it => it['id'];
  const createConfigurationEntity = (id: string, name = '') =>
    ({
      id,
      name: name || `name-${id}`,
    } as ConfigurationEntity);

  let state;

  beforeEach(() => {
    state = {
      configuration: configurationAdapter.setAll(
        [
          createConfigurationEntity('PRODUCT-AAA'),
          createConfigurationEntity('PRODUCT-BBB'),
          createConfigurationEntity('PRODUCT-CCC'),
        ],
        {
          ...initialConfigState,
          selectedId: 'PRODUCT-BBB',
          error: ERROR_MSG,
          loaded: true,
        }
      ),
    };
  });

  describe('Configuration Selectors', () => {
    it('getAllConfiguration() should return the list of Configuration', () => {
      const results = ConfigurationSelectors.getAllConfiguration(state);
      const selId = getConfigurationId(results[1]);

      expect(results.length).toBe(3);
      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getSelected() should return the selected Entity', () => {
      const result = ConfigurationSelectors.getSelectedConfig(state);
      const selId = getConfigurationId(result);

      expect(selId).toBe('PRODUCT-BBB');
    });

    it('getConfigurationLoaded() should return the current \'loaded\' status', () => {
      const result = ConfigurationSelectors.getConfigurationLoaded(state);

      expect(result).toBe(true);
    });

    it('getConfigurationError() should return the current \'error\' state', () => {
      const result = ConfigurationSelectors.getConfigurationError(state);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
