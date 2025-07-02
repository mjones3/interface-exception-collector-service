import { TestBed } from '@angular/core/testing';

import { IrradiationService } from './irradiation.service';

describe('IrradiationService', () => {
  let service: IrradiationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(IrradiationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
