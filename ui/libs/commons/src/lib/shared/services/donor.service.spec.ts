import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DonorService } from './donor.service';

describe('DonorService', () => {
  let service: DonorService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(DonorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
