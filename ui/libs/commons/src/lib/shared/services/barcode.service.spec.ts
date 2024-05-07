import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { BarcodeService } from './barcode.service';

describe('BarcodeService', () => {
  let service: BarcodeService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(BarcodeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
