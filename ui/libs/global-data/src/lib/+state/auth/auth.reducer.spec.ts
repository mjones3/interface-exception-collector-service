import * as AuthActions from './auth.actions';
import { AuthEntity } from './auth.models';
import { authInitialState, authReducerFactory, AuthState } from './auth.reducer';

describe('Auth Reducer', () => {
  const createAuthEntity = (id: string, name = '') =>
    ({
      id,
      name: name || `name-${id}`,
    } as AuthEntity);

  beforeEach(() => {});

  describe('valid Auth actions', () => {
    it('loadAuthSuccess should return set the list of known Auth', () => {
      const auth = createAuthEntity('PRODUCT-AAA');
      const action = AuthActions.loadAuthSuccess({ auth });

      const result: AuthState = authReducerFactory(authInitialState, action);

      expect(result.loaded).toBe(true);
    });
  });

  describe('unknown action', () => {
    it('should return the previous state', () => {
      const action = {} as any;

      const result = authReducerFactory(authInitialState, action);

      expect(result).toBe(authInitialState);
    });
  });
});
