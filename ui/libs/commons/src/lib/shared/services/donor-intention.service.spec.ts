import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DonorIntentionService } from './donor-intention.service';

describe('DonorIntentionService', () => {
  let httpMock: HttpTestingController;
  let service: DonorIntentionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DonorIntentionService, ...getAppInitializerMockProvider('commons-lib')],
      imports: [HttpClientTestingModule],
    });

    //Mock Dependent Classes
    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(DonorIntentionService);
  });

  it('should get unitNumber validations', () => {
    expect(service).toBeTruthy();

    // //Mock Data
    // const mockCheckInRequest: any = checkInRequestData;
    // const mockCheckInResponse: any = checkInResponseData;
    //
    // //Method To Test
    // service.validateUnitNumber(mockCheckInRequest).subscribe((response) => {
    //   expect(response).not.toBe(null);
    //   expect(response).toEqual(mockCheckInResponse);
    // });
    //
    // const request = httpMock.expectOne('api/v1/inventories/check-in');
    // expect(request.request.method).toBe('POST');
    // request.flush(mockCheckInResponse);
  });
});
