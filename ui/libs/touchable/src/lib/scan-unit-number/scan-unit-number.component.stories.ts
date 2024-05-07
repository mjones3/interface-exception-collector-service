import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { TouchableComponentsModule } from '../touchable-components.module';
import { ScanUnitNumberComponent } from './scan-unit-number.component';

export default {
  title: 'rsa-scan-unit-number'
};

export const primary = () => ({
  moduleMetadata: {
    imports: [BrowserAnimationsModule, TouchableComponentsModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({ loader: { provide: TranslateLoader, useClass: TranslateFakeLoader } })]
  },
  component: ScanUnitNumberComponent
});
