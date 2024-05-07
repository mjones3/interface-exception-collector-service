import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { getAppInitializerMockProvider, PageHistoryService } from '@rsa/commons';

describe('PageHistoryService', () => {
  let service: PageHistoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PageHistoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
