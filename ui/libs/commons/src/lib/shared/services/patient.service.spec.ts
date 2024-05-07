import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider, PatientDto } from '@rsa/commons';
import { PatientService } from './patient.service';

describe('PatientService', () => {
  let service: PatientService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PatientService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  //TODO: Fix it
  it.skip('should get patients based on criteria', done => {
    const params = { size: 100 };

    service.getPatientByCriteria(params).subscribe(value => {
      expect(value).toBeInstanceOf<PatientDto[]>(value);
      expect(value?.length).toBeGreaterThan(0);
      done();
    });

    const req = httpMock.expectOne(`${service.patientEndpoint}?size=${params.size}`);
    expect(req.request.method).toEqual('GET');
  });
});
