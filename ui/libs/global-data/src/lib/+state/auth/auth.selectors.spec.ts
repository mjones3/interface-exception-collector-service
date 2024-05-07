import * as AuthSelectors from './auth.selectors';

describe('Auth Selectors', () => {
  const ERROR_MSG = 'No Error Available';
  const getAuthId = it => it['id'];

  let state;

  beforeEach(() => {
    state = {
      auth: {
        user: null,
        loaded: false
      }
    };
  });

  describe('Auth Selectors', () => {

    it('getAuthLoaded() should return the current \'loaded\' status', () => {
      const result = AuthSelectors.getAuthLoaded(state);

      expect(result).toBe(true);
    });

    it('getAuthError() should return the current \'error\' state', () => {
      const result = AuthSelectors.getAuthError(state);

      expect(result).toBe(ERROR_MSG);
    });
  });
});
