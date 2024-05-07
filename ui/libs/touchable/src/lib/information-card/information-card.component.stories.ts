import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { number, object, text } from '@storybook/addon-knobs';
import { TouchableComponentsModule } from '../touchable-components.module';
import { InformationCardComponent } from './information-card.component';

export default {
  title: 'rsa-information-card'
};

export const primary = () => ({
  moduleMetadata: {
    imports: [BrowserAnimationsModule, TouchableComponentsModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({ loader: { provide: TranslateLoader, useClass: TranslateFakeLoader } })]
  },
  component: InformationCardComponent,
  props: {
    title: text('title', 'Descriptions'),
    maxRows: number('maxRows', 3),
    descriptions: object('descriptions', [
      { label: 'Label 1', value: 'W123456789876' },
      { label: 'Label 2', value: 'Lorem Ipsum' },
      { label: 'Label 3', value: 'Lorem Ipsum' },
      { label: 'Label 4', value: 'Lorem Ipsum' },
      { label: 'Label 5', value: 'Lorem Ipsum' },
      { label: 'Label 6', value: 'Lorem Ipsum' },
      { label: 'Label 7', value: 'Lorem Ipsum' },
      { label: 'Label 8', value: 'Lorem Ipsum' },
      { label: 'Label 9', value: 'Lorem Ipsum' }
    ])
  }
});
