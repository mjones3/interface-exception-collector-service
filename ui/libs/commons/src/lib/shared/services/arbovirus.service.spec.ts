import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ArbovirusService, getAppInitializerMockProvider, toasterMockProvider } from '@rsa/commons';

describe('ArbovirusService', () => {
  let httpMock: HttpTestingController;
  let service: ArbovirusService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib'), ...toasterMockProvider],
    });

    service = TestBed.inject(ArbovirusService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
