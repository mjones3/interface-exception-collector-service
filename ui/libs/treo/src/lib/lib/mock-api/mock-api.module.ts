import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { APP_INITIALIZER, ModuleWithProviders, NgModule } from '@angular/core';
import { TreoMockApiInterceptor } from './mock-api.interceptor';
import { TreoMockApiService } from './mock-api.service';

// @dynamic
@NgModule({
  providers: [
    TreoMockApiService,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TreoMockApiInterceptor,
      multi: true
    }
  ]
})
export class TreoMockApiModule {
  /**
   * forRoot method for setting user configuration
   *
   * @param mockDataServices
   */
  static forRoot(mockDataServices: any[]): ModuleWithProviders<TreoMockApiModule> {
    return {
      ngModule: TreoMockApiModule,
      providers: [
        {
          provide: APP_INITIALIZER,
          deps: mockDataServices,
          useFactory: () => () => null,
          multi: true
        }
      ]
    };
  }
}
