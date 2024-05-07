import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { boolean, object, text } from '@storybook/addon-knobs';
import { optionsMock } from '../../shared/testing/mocks/data/shared.mock';
import { TouchableComponentsModule } from '../touchable-components.module';
import { OptionsPickerComponent } from './options-picker.component';

export default {
  title: 'rsa-options-picker'
};

export const transparentHeaderSingleSelection = () => ({
  moduleMetadata: {
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule,
      RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })
    ]
  },
  component: OptionsPickerComponent,
  props: {
    headerTitle: text('headerTitle', 'Select Product Type'),
    transparentHeader: boolean('transparentHeader', true),
    options: object<any>('options', optionsMock),
    optionsLabel: text('optionsLabel', 'name')
  }
});

export const whiteHeaderMultipleSelection = () => ({
  moduleMetadata: {
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule,
      RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })
    ]
  },
  component: OptionsPickerComponent,
  props: {
    headerTitle: text('headerTitle', 'Select Product Type'),
    transparentHeader: boolean('transparentHeader', false),
    options: object<any>('options', optionsMock),
    optionsLabel: text('optionsLabel', 'name'),
    multiple: boolean('multiple', true)
  }
});
