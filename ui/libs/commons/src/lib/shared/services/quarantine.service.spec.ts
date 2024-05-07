import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { QuarantineService } from './quarantine.service';

describe('QuarantineService', () => {
  let service: QuarantineService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(QuarantineService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
