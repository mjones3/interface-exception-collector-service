import { TestBed } from '@angular/core/testing';

import { provideMockActions } from '@ngrx/effects/testing';
import { provideMockStore } from '@ngrx/store/testing';

import { DataPersistence, NxModule } from '@nrwl/angular';

import { EMPTY, Observable } from 'rxjs';
import { FacilityEffects } from './facility.effects';

describe('FacilityEffects', () => {
  const actions: Observable<any> = EMPTY;
  let effects: FacilityEffects;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [NxModule.forRoot()],
      providers: [FacilityEffects, DataPersistence, provideMockActions(() => actions), provideMockStore()],
    });

    effects = TestBed.inject(FacilityEffects);
  });

  it('should be truthy', function () {
    expect(effects).toBeTruthy();
  });
});
