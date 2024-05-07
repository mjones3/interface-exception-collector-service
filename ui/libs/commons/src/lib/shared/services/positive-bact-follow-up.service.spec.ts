import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider, PositiveBactFollowUpService } from '@rsa/commons';

describe('PositiveBactFollowUpService', () => {
  let service: PositiveBactFollowUpService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PositiveBactFollowUpService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
