import { createAction, props } from '@ngrx/store';
import { AuthEntity } from './auth.models';

export const initAuth = createAction('[Auth Page] Init');

export const loadAuthSuccess = createAction('[Auth/API] Load Auth Success', props<{ auth: AuthEntity }>());

export const removeAuthSuccess = createAction('[Auth/API] Remove Auth Success');

export const loadAuthFailure = createAction('[Auth/API] Load Auth Failure', props<{ error: any }>());
