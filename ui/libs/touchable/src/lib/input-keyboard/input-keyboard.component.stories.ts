import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { text } from '@storybook/addon-knobs';

import { TouchableComponentsModule } from '../touchable-components.module';

export default {
  title: 'rsa-input-keyboard'
};

export const primary = () => ({
  moduleMetadata: {
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: {
          provide: TranslateLoader,
          useClass: TranslateFakeLoader
        }
      })]
  },
  template: `<div class="flex justify-center items-center h-full w-full">
                <div class="flex">
                <rsa-input-keyboard #ik [labelTitle]="labelTitle"
                                    [labelClasses]="labelClasses"
                                    [keyboardType]="keyboardType"
                                    [placeholder]="placeholder"
                                    [labelWidth]="labelWidth"
                                    [iconName]="iconName"
                                    regex="^[0-9a-zA-Z ]+$"
                                    patternMessage="Value must be alphanumeric"
                                    >
                </rsa-input-keyboard></div></div>
              <ng-template #inputTpl let-value="value" let-onInputChange="onInputChange">
                <mat-form-field class="flex-auto treo-mat-no-subscript">
                    <input matInput [(ngModel)]="value" placeholder="Custom template input" autocomplete="off"
                      rsaMaskRegex allowedCharsRegex="[0-9a-zA-Z]+" (input)="onInputChange($event)" pattern="[0-9a-zA-Z]+"/>
                </mat-form-field>
              </ng-template>`,
  props: {
    labelTitle: text('labelTitle', 'Label Name'),
    labelClasses: text('labelClasses', ''),
    labelWidth: text('labelWidth', ''),
    keyboardType: text('keyboardType', ''),
    placeholder: text('placeholder', ''),
    iconName: text('iconName', 'opacity')
  }
});
