import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { PhysicalPulseService } from './physical-pulse.service';

describe('PhysicalPulseService', () => {
  let service: PhysicalPulseService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PhysicalPulseService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
