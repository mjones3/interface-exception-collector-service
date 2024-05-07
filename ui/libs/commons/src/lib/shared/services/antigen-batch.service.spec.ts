import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider, toasterMockProvider } from '@rsa/commons';
import { AntigenBatchService } from './antigen-batch.service';

describe('BatchService', () => {
  let httpMock: HttpTestingController;
  let service: AntigenBatchService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib'), ...toasterMockProvider],
    });

    service = TestBed.inject(AntigenBatchService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
