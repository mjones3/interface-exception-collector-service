import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { DriveService } from './drive.service';

describe('DriveService', () => {
  let httpMock: HttpTestingController;
  let service: DriveService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, RouterTestingModule],
      providers: [DriveService, ...getAppInitializerMockProvider('commons-lib')],
    });

    service = TestBed.inject(DriveService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
