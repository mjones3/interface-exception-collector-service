import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MaterialModule } from '@rsa/material';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { SterileConnectionService } from './sterile-connection.service';

describe('SterileConnectionService', () => {
  let service: SterileConnectionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, MaterialModule],
      providers: [SterileConnectionService, ...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(SterileConnectionService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
