import { TestBed } from '@angular/core/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { ToastrModule } from 'ngx-toastr';
import { toasterMockProvider } from '../../data/mock/toaster.mock';
import { ToastrImplService } from './toastr-impl.service';

describe('ToastrImplService', () => {
  let service: ToastrImplService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        ToastrModule.forRoot({}),
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [...toasterMockProvider],
    });
    service = TestBed.inject(ToastrImplService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
