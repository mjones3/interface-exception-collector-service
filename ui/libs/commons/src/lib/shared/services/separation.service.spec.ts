import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { EnvironmentConfigService } from './environment-config.service';
import { SeparationService } from './separation.service';

describe('SeparationService', () => {
  let service: SeparationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EnvironmentConfigService, ...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(SeparationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
