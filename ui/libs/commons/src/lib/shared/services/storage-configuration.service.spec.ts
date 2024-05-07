import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { getAppInitializerMockProvider, StorageConfigurationService } from '@rsa/commons';

describe('StorageConfigurationService', () => {
  let service: StorageConfigurationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(StorageConfigurationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
