import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DonorEligibilityService } from './donor-eligibility.service';

describe('DonorEligibilityService', () => {
  let service: DonorEligibilityService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(DonorEligibilityService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
