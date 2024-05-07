import { TestBed } from '@angular/core/testing';

import { HttpClientTestingModule } from '@angular/common/http/testing';
import { getAppInitializerMockProvider } from '@rsa/commons';
import { TransferReceiptService } from './transfer-receipt.service';

describe('TransferReceiptService', () => {
  let service: TransferReceiptService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(TransferReceiptService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
