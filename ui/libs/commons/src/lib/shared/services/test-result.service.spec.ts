import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '@rsa/commons';
import { TestResultService } from './test-result.service';

describe('TestResultService', () => {
  let service: TestResultService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(TestResultService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
