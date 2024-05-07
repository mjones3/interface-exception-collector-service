import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { EnvironmentConfigService } from './environment-config.service';
import { ReasonService } from './reason.service';

describe('ReasonService', () => {
  let httpMock: HttpTestingController;
  let service: ReasonService;
  let envConfig: EnvironmentConfigService;

  //Before
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [ReasonService, ...getAppInitializerMockProvider('commons-lib')],
      imports: [HttpClientTestingModule],
    });

    //Mock Dependent Classes
    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(ReasonService);
    envConfig = TestBed.inject(EnvironmentConfigService);
  });

  //After
  afterEach(() => {});

  //Test or Spec
  it('should get reasons', () => {
    expect(service).toBeTruthy();

    //Mock Data
    const mockReasons: any = [
      { name: 'POSITIVE HBSAG', selectionKey: '0' },
      { name: 'SARS 28 DAY DISCARD', selectionKey: '1' },
    ];

    //Method To Test
    service.getReasons().subscribe(response => {
      expect(response).not.toBe(null);
      expect(response).toEqual(mockReasons);
    });

    const request = httpMock.expectOne(
      `${envConfig.env.serverApiURL}/v1/discard-reasons?process=discard&page=0&size=100`
    );
    expect(request.request.method).toBe('GET');
    request.flush(mockReasons);
  }); //End Test
});
