import { TestBed } from '@angular/core/testing';

import { StorageInitialFreezerService } from './storage-initial-freezer.service';

describe('StorageInitialFreezerService', () => {
  let service: StorageInitialFreezerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(StorageInitialFreezerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
