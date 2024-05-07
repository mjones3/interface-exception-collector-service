import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { boolean, text } from '@storybook/addon-knobs';
import { toasterProvidersMock } from '../../shared/testing/mocks/toaster.mock';
import { TouchableComponentsModule } from '../touchable-components.module';
import { UnitObjectForBatchScreenComponent } from './unit-object-for-batch-screen.component';

export default {
  title: 'rsa-unit-object-for-batch-screen'
};

export const primary = () => ({
  moduleMetadata: {
    imports: [BrowserAnimationsModule, TouchableComponentsModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })],
    providers: [...toasterProvidersMock]
  },
  component: UnitObjectForBatchScreenComponent,
  props: {
    headerTitle: text('headerTitle', 'Added Units'),
    headerSubTitle: text('headerSubTitle', 'Add SubTitle'),
    labelWidth: text('labelsWidthClass', ''),
    showHeaderSubTitle: boolean('showHeaderSubTitle', false),
    showCentrifugeCode: boolean('showCentrifugeCode', false)
  }
});
