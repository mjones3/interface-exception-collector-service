import { createFeatureSelector, createSelector } from '@ngrx/store';
import { AUTH_FEATURE_KEY, AuthPartialState, AuthState } from './auth.reducer';

// Lookup the 'Auth' feature state managed by NgRx
export const getAuthState = createFeatureSelector<AuthPartialState, AuthState>(AUTH_FEATURE_KEY);

export const getAuthLoaded = createSelector(getAuthState, (state: AuthState) => state.loaded);

export const getAuthError = createSelector(getAuthState, (state: AuthState) => state.error);

export const getAuthUser = createSelector(getAuthState, (state: AuthState) => state?.user);

export const getAuthUserId = createSelector(getAuthState, (state: AuthState) => state?.user?.id);
