import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { object, text } from '@storybook/addon-knobs';
import { optionsMock } from '../../shared/testing/mocks/data/shared.mock';
import { TouchableComponentsModule } from '../touchable-components.module';
import { OptionsPickerDialogComponent } from './options-picker-dialog.component';

export default {
  title: 'rsa-options-picker-dialog'
};

@Component({
  template: `
    <div class="flex justify-center items-center h-full w-full">
      <button mat-raised-button color="primary" (click)="openModal()">Launch Options Picker Modal</button>
    </div>`
})
class WrapperComponent {

  @Input() dialogTitle: string;
  @Input() cancelButtonText: string;
  @Input() options: any;
  @Input() optionsLabel: any;

  constructor(private matDialog: MatDialog) {
  }

  openModal(): void {
    const dialogRef = this.matDialog.open(OptionsPickerDialogComponent,
      {
        data: {
          dialogTitle: this.dialogTitle,
          cancelButtonText: this.cancelButtonText,
          options: this.options,
          optionsLabel: this.optionsLabel
        }
      });
    dialogRef.afterClosed().subscribe(result => {
      console.log('Modal Closed with result: ', result);
    });
  }
}

export const primary = () => ({
  moduleMetadata: {
    declarations: [WrapperComponent],
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({ loader: { provide: TranslateLoader, useClass: TranslateFakeLoader } })]
  },
  component: WrapperComponent,
  props: {
    dialogTitle: text('dialogTitle', 'Select an option'),
    cancelButtonText: text('cancelButtonText', 'Cancel'),
    options: object<any>('options', optionsMock),
    optionsLabel: text('optionsLabel', 'name')
  }
});


