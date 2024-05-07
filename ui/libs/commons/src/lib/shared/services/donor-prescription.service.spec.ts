import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';

import { DonorPrescriptionService } from './donor-prescription.service';

describe('DonorPrescriptionService', () => {
  let service: DonorPrescriptionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(DonorPrescriptionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
