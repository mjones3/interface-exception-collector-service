import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';

import { FacilitiesModalService } from './facilities-modal.service';

describe('FacilitiesModalService', () => {
  let service: FacilitiesModalService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FacilitiesModalService, ...getAppInitializerMockProvider('commons-lib')]
    });
    service = TestBed.inject(FacilitiesModalService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
