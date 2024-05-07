import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { LookUpService } from './look-up.service';

describe('LookUpService', () => {
  let service: LookUpService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });

    service = TestBed.inject(LookUpService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
