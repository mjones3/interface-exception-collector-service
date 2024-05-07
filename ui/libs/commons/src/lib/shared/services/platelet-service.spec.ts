import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider, PlateletService } from '@rsa/commons';

describe('PlateletService', () => {
  let service: PlateletService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PlateletService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
