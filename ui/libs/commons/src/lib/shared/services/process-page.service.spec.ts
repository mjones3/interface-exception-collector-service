import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { ProcessPageService } from './process-page.service';

describe('ProcessPageService', () => {
  let httpMock: HttpTestingController;
  let service: ProcessPageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });


    service = TestBed.inject(ProcessPageService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });


  //Create Inventory
  // it('should create an inventory', () => {
  //   service.createProcess(null).subscribe(
  //     (response) => {
  //       expect(response.body).not.toBe(null);
  //       expect(response.body).toEqual(_processModuleData);
  //     }
  //   );
  //
  //   const request = httpMock.expectOne('http://localhost/api/v1/processes/pages');
  //   expect(request.request.method).toEqual('POST');
  // });
});
