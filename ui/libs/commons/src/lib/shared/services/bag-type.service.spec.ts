import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { BagTypeService } from './bag-type.service';

describe('BagTypeService', () => {
  let httpMock: HttpTestingController;
  let service: BagTypeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BagTypeService, ...getAppInitializerMockProvider('commons-lib')],
      imports: [HttpClientTestingModule, TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useClass: TranslateFakeLoader
        }
      })],
    });

    //Mock Dependent Classes
    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(BagTypeService);
  });

  it('should get unitNumber validations', () => {
    expect(service).toBeTruthy();
    //
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
