import { TestBed } from '@angular/core/testing';

import { DefaultErrorHandlerService } from './default-error-handler.service';

describe('DefaultErrorHandlerService', () => {
  let service: DefaultErrorHandlerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DefaultErrorHandlerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
