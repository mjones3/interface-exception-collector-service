import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { QualityControlService } from './quality-control.service';

describe('DrawService', () => {
  let httpMock: HttpTestingController;
  let service: QualityControlService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [QualityControlService, ...getAppInitializerMockProvider('commons-lib')]
    });

    service = TestBed.inject(QualityControlService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
