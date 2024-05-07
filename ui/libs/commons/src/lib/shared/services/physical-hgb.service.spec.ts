import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { PhysicalHGBService } from './physical-hgb.service';

describe('PhysicalHgbService', () => {
  let service: PhysicalHGBService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PhysicalHGBService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
