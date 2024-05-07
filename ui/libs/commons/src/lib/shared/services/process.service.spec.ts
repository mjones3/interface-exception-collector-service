import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { ProcessService } from './process.service';

describe('ProcessService', () => {
  let service: ProcessService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(ProcessService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /*it('should get process configuration', (done) => {
    const config = {key: 'value'};
    const uuid = 'd938e76c-a152-49d1-95d9-c3fb381a32dd';
    service.getProcessConfiguration(uuid).subscribe((conf) => {
      expect(conf).toEqual(config);
      done();
    });
    const req = httpMock.expectOne(`/v1/processes/products/${uuid}`);
    expect(req.request.method).toEqual('GET');
    req.flush(config);
  });*/
});
