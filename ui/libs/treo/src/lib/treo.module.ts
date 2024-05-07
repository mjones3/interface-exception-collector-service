import { NgModule, Optional, SkipSelf } from '@angular/core';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { TreoCardModule } from './components/card/card.module';
import { TreoScrollbarModule } from './directives/scrollbar/scrollbar.module';
import { TreoMediaWatcherModule } from './services/media-watcher/media-watcher.module';
import { TreoSplashScreenModule } from './services/splash-screen/splash-screen.module';

@NgModule({
  imports: [TreoCardModule, TreoMediaWatcherModule, TreoScrollbarModule, TreoSplashScreenModule],
  providers: [
    {
      // Use the 'fill' appearance on form fields by default
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        appearance: 'fill',
      },
    },
  ],
  exports: [TreoCardModule, TreoMediaWatcherModule, TreoScrollbarModule, TreoSplashScreenModule],
})
export class TreoModule {
  /**
   * Constructor
   *
   * @param parentModule
   */
  constructor(@Optional() @SkipSelf() parentModule?: TreoModule) {
    if (parentModule) {
      throw new Error('TreoModule has already been loaded. Import this module in the AppModule only!');
    }
  }
}
