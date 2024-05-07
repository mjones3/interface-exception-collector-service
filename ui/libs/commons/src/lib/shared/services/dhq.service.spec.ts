import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DhqService } from './dhq.service';

describe('DhqService', () => {
  let service: DhqService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(DhqService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
