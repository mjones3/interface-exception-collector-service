import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { map } from 'rxjs/operators';

import * as AuthActions from './auth.actions';

@Injectable()
export class AuthEffects {
  init$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.initAuth),
      map(() => AuthActions.loadAuthSuccess({ auth: {} }))
    )
  );

  constructor(private actions$: Actions) {}
}
