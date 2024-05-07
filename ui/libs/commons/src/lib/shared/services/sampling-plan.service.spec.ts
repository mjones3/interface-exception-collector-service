import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '@rsa/commons';
import { SamplingPlanService } from './sampling-plan.service';

describe('SamplingPlanService', () => {
  let service: SamplingPlanService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(SamplingPlanService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
