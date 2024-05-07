import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider, PqaService } from '@rsa/commons';

describe('PqaService', () => {
  let service: PqaService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PqaService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
