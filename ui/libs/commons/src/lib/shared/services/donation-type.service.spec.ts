import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DonationTypeService } from './donation-type.service';

describe('DonationTypeService', () => {
  let httpMock: HttpTestingController;
  let service: DonationTypeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [DonationTypeService, ...getAppInitializerMockProvider('commons-lib')],
      imports: [HttpClientTestingModule],
    });

    //Mock Dependent Classes
    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(DonationTypeService);
  });

  it('should get unitNumber validations', () => {
    expect(service).toBeTruthy();

    //Mock Data
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
