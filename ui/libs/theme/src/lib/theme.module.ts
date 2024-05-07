import { CommonModule } from '@angular/common';
import { ModuleWithProviders, NgModule } from '@angular/core';
import { TranslateModule } from '@ngx-translate/core';
import { AuthModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { TreoScrollbarModule } from '@treo';
import { TabLayoutModule } from './layout/layouts/general/tab-layout/tab-layout.module';
import { BasicLayoutModule } from './layout/layouts/vertical/basic/basic.module';
import { UserModule } from './layout/user/user.module';

@NgModule({
  declarations: [],
  imports: [
    CommonModule,
    AuthModule,
    MaterialModule,
    BasicLayoutModule,
    TabLayoutModule,
    TreoScrollbarModule,
    TranslateModule,
    UserModule,
  ],
  exports: [MaterialModule, BasicLayoutModule, UserModule],
})
export class ThemeModule {
  /**
   * forRoot method for setting user configuration
   *
   * @param config
   */
  static forRoot(): ModuleWithProviders<ThemeModule> {
    return {
      ngModule: ThemeModule,
      providers: [],
    };
  }
}
