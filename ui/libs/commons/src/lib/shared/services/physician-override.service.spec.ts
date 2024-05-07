import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '@rsa/commons';
import { PhysicianOverrideService } from './physician-override.service';

describe('PhysicianOverrideService', () => {
  let service: PhysicianOverrideService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(PhysicianOverrideService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
