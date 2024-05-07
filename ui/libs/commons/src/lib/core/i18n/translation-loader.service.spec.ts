import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { EnvironmentConfigService } from '@rsa/commons';
import { TranslationLoaderService } from './translation-loader.service';

describe('TranslationService', () => {
  let service: TranslationLoaderService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EnvironmentConfigService],
    });
    service = TestBed.inject(TranslationLoaderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
