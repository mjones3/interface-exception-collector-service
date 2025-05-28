import { Action, createReducer, on } from '@ngrx/store';
import * as AuthActions from './auth.actions';
import { KeycloakProfile } from 'keycloak-js';

export const AUTH_FEATURE_KEY = 'auth';

export interface AuthState {
  id?: string;
  loaded: boolean; // has the Auth list been loaded
  error?: string | null; // last known error (if any)
  user?: KeycloakProfile; // Logged in user
  accessToken?: string; // Access Token
}

export interface AuthPartialState {
  readonly [AUTH_FEATURE_KEY]: AuthState;
}

export const authInitialState: AuthState = {
  // set initial required properties
  id: '',
  loaded: false,
  user: null,
  accessToken: ''
};

const authReducer = createReducer(
  authInitialState,
  on(AuthActions.initAuth, state => ({ ...state, loaded: false, error: null })),
  on(AuthActions.loadAuthSuccess, (state, { auth }) =>
    ({ ...state, ...auth, id: auth?.user?.id, loaded: true })),
  on(AuthActions.removeAuthSuccess, () => ({ ...authInitialState, loaded: false })),
  on(AuthActions.loadAuthFailure, (state, { error }) => ({ ...state, error }))
);

export function authReducerFactory(state: AuthState | undefined, action: Action) {
  return authReducer(state, action);
}
