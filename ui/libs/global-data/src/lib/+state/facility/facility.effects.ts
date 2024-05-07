import { Injectable } from '@angular/core';
import { Actions, createEffect, ofType } from '@ngrx/effects';
import { map } from 'rxjs/operators';

import * as FacilityActions from './facility.actions';

@Injectable()
export class FacilityEffects {
  init$ = createEffect(() =>
    this.actions$.pipe(
      ofType(FacilityActions.initFacility),
      map(() => FacilityActions.loadFacilitySuccess({ facility: [] }))
    )
  );

  constructor(private actions$: Actions) {}
}
