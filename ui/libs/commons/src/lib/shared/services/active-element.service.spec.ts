import { TestBed } from '@angular/core/testing';

import { ActiveElementService } from './active-element.service';

describe('ActiveElementService', () => {
  let service: ActiveElementService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ActiveElementService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
