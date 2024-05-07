import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider, PqcService } from '@rsa/commons';

describe('PqcService', () => {
  let service: PqcService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PqcService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
