import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider, SpecialTestingConfigurationService } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';

describe('SpecialTestingConfigurationService', () => {
  let service: SpecialTestingConfigurationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MaterialModule],
      providers: [SpecialTestingConfigurationService, ...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(SpecialTestingConfigurationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
