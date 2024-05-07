import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { QuarantineReasonsService } from './quarantine-reasons.service';

describe('QuarantineReasonsService', () => {
  let service: QuarantineReasonsService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(QuarantineReasonsService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
