import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { getAppInitializerMockProvider, LocationTypeService } from '@rsa/commons';

describe('LocationTypeService', () => {
  let service: LocationTypeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(LocationTypeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
