import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';

import { PgdService } from './pgd.service';

describe('PgdService', () => {
  let service: PgdService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PgdService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
