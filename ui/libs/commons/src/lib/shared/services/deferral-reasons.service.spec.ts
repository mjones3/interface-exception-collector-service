import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { getAppInitializerMockProvider } from '@rsa/commons';
import { DeferralReasonsService } from './deferral-reasons.service';

describe('DeferralReasonsService', () => {
  let service: DeferralReasonsService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });

    service = TestBed.inject(DeferralReasonsService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
