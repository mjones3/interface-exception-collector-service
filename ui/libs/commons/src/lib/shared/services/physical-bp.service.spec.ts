import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { PhysicalBPService } from './physical-bp.service';

describe('PhysicalBpService', () => {
  let service: PhysicalBPService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PhysicalBPService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
