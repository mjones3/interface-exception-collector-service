import { TestBed } from '@angular/core/testing';

import { CustomMissingTranslationHandlerService } from './custom-missing-translation-handler.service';

describe('CustomMissingTranslationHandlerService', () => {
  let service: CustomMissingTranslationHandlerService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CustomMissingTranslationHandlerService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
