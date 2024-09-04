import { TestBed } from '@angular/core/testing';

import { DiscardService } from './discard.service';

describe('DiscardService', () => {
  let service: DiscardService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DiscardService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
