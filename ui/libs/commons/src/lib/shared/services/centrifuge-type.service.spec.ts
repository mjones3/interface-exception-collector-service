import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { CentrifugeTypeService } from './centrifuge-type.service';

describe('CentrifugeTypeService', () => {
  let service: CentrifugeTypeService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(CentrifugeTypeService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
