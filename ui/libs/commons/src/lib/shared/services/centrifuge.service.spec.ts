import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { CentrifugeService } from './centrifuge.service';

describe('CentrifugeService', () => {
  let service: CentrifugeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(CentrifugeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
