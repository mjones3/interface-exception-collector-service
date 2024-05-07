import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { getAppInitializerMockProvider } from '../../data/mock/environment-config.mock';
import { BagKitUsageService } from './bag-kit-usage.service';

describe('BagKitUsageService', () => {
  let service: BagKitUsageService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [BagKitUsageService, ...getAppInitializerMockProvider('commons-lib')],
      imports: [
        HttpClientTestingModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
    });

    service = TestBed.inject(BagKitUsageService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
