import { Component, Input } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { object, text } from '@storybook/addon-knobs';
import { donationTypesMock, donorIntentionsMock } from '../../shared/testing/mocks/data/shared.mock';
import { TouchableComponentsModule } from '../touchable-components.module';

export default {
  title: 'select-options-picker'
};

// Component Wrapper
@Component({
  template: `
    <div class="flex justify-center items-center h-full w-full">
      <rsa-select-options-picker [labelTitle]="'Label Title'" [options]="selectOptionsData" [optionsLabel]="'name'"
                                 [dialogTitle]="dialogTitle" [placeholder]="'Select Donor Intention'"
                                 [selectId]="'donorIntentionSelect'"></rsa-select-options-picker>
    </div>`
})
class SelectOptionsPickerWrapperComponent {

  @Input() selectOptionsData: any;
  @Input() dialogTitle: string;

  constructor() {
  }
}

export const selectWithPicker = () => ({
  moduleMetadata: {
    declarations: [SelectOptionsPickerWrapperComponent],
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })]
  },
  component: SelectOptionsPickerWrapperComponent,
  props: {
    selectOptionsData: object<any>('selectOptionsData', donorIntentionsMock),
    dialogTitle: text('dialogTitle', 'Select Donation Intention')
  }
});

export const selectWithFilter = () => ({
  moduleMetadata: {
    declarations: [SelectOptionsPickerWrapperComponent],
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })]
  },
  component: SelectOptionsPickerWrapperComponent,
  props: {
    selectOptionsData: object<any>('selectOptionsData', donationTypesMock),
    dialogTitle: text('dialogTitle', 'Select Donation Types')
  }
});


