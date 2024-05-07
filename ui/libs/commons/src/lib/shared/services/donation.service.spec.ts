import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DonationService } from './donation.service';

describe('DonationService', () => {
  let httpMock: HttpTestingController;
  let service: DonationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DonationService, ...getAppInitializerMockProvider('commons-lib')]
    });


    service = TestBed.inject(DonationService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  //Create Inventory
  // it('should create an inventory', () => {
  //   service.createDonation(null).subscribe(
  //     (response) => {
  //       expect(response.body).not.toBe(null);
  //       expect(response.body).toEqual(_donationData);
  //     }
  //   );
  //
  //   const request = httpMock.expectOne('http://localhost/api/v1/donations');
  //   expect(request.request.method).toEqual('POST');
  // });
});
