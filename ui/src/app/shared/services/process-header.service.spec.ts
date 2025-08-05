import { TestBed } from '@angular/core/testing';
import { ProcessHeaderService } from './process-header.service';

describe('ProcessHeaderService', () => {
  let service: ProcessHeaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ProcessHeaderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
