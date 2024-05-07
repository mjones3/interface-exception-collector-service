import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EnvironmentConfigService } from './environment-config.service';
import { ShipmentService } from './shipment.service';

describe('ShipmentService', () => {
  let service: ShipmentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        {
          provide: EnvironmentConfigService,
          useValue: { env: { serverApiURL: '' } },
        },
      ],
    });
    service = TestBed.inject(ShipmentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
