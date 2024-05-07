import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DonorConsentTypeService } from './donor-consent-type.service';

describe('DonorConsentTypeService', () => {
  let service: DonorConsentTypeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(DonorConsentTypeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
