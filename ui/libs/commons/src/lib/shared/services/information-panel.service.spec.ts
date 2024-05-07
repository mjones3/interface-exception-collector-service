import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialogModule } from '@angular/material/dialog';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule, TranslateService } from '@ngx-translate/core';
import { getAppInitializerMockProvider, toasterMockProvider } from '@rsa/commons';
import { InformationPanelService } from './information-panel.service';

describe('InformationPanelService', () => {
  let service: InformationPanelService;
  let translateService: TranslateService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule,
        MatDialogModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [...toasterMockProvider, ...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(InformationPanelService);
    translateService = TestBed.inject(TranslateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should checkMultipleLabels on setDescriptions', () => {
    spyOn(service, 'checkMultipleLabels');
    service.setDescriptions({ label: 'a.label' }, { label: 'b.label' }, { label: 'c.label' });
    expect(service.checkMultipleLabels).toHaveBeenCalled();
  });
});
