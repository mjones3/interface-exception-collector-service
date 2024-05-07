import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DonationService } from './donation.service';
import { RogueService } from './rogue.service';

describe('RogueService', () => {
  let httpMock: HttpTestingController;
  let service: RogueService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [DonationService, ...getAppInitializerMockProvider('commons-lib')]
    });


    service = TestBed.inject(RogueService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  /*//Create Rogue
  it('should create a Rogue', () => {
    service.createRogue(null).subscribe(
      (response) => {
        expect(response.body).not.toBe(null);
        expect(response.body).toEqual(_rogueData);
      }
    );

    const request = httpMock.expectOne('http://localhost/api/v1/rogues');
    expect(request.request.method).toEqual('POST');
  });*/
});
