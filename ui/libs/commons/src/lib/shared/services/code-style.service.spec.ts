import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { CodeStyleService } from './code-style.service';

describe('CodeStyleService', () => {
  let service: CodeStyleService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });

    service = TestBed.inject(CodeStyleService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
