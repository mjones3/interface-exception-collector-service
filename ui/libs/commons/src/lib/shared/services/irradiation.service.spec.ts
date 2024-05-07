import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { IrradiationService } from '.';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';

describe('IrradiationService', () => {
  let service: IrradiationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(IrradiationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
