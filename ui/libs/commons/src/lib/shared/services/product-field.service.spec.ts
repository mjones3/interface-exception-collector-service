import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '@rsa/commons';
import { ProductFieldService } from './product-field.service';

describe('ProductFieldService', () => {
  let service: ProductFieldService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(ProductFieldService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
