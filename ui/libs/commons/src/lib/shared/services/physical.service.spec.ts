import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { PhysicalService } from './physical.service';

describe('PhysicalService', () => {
  let service: PhysicalService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PhysicalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
