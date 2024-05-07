import * as ConfigurationActions from './configuration.actions';
import { ConfigurationEntity } from './configuration.models';
import { ConfigState, configurationReducerFactory, initialConfigState } from './configuration.reducer';

describe('Configuration Reducer', () => {
  const createConfigurationEntity = (id: string, name = '') =>
    ({
      id,
      name: name || `name-${id}`,
    } as ConfigurationEntity);

  beforeEach(() => {});

  describe('valid Configuration actions', () => {
    it('loadConfigurationSuccess should return set the list of known Configuration', () => {
      const configuration = createConfigurationEntity('PRODUCT-AAA');
      const action = ConfigurationActions.loadConfigurationSuccess({ configuration });
      const result: ConfigState = configurationReducerFactory(initialConfigState, action);
      expect(result.loaded).toBe(true);
    });
  });

  describe('unknown action', () => {
    it('should return the previous state', () => {
      const action = {} as any;

      const result = configurationReducerFactory(initialConfigState, action);

      expect(result).toBe(initialConfigState);
    });
  });
});
