import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MaterialModule } from '@rsa/material';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { FacilityService } from './facility.service';

describe('FacilityService', () => {
  let httpMock: HttpTestingController;
  let service: FacilityService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MaterialModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });

    service = TestBed.inject(FacilityService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
