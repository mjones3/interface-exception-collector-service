import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { object, text } from '@storybook/addon-knobs';
import { isNotNullOrUndefined } from 'codelyzer/util/isNotNullOrUndefined';
import { TouchableComponentsModule } from '../touchable-components.module';
import { FilterableDropDownComponent } from './filterable-drop-down.component';

export default {
  title: 'rsa-filterable-drop-down'
};

// Component Wrapper
@Component({
  template: `
    <div class="flex justify-center items-center h-full w-full">
      <button mat-raised-button color="primary" (click)="openFilterableDD()">Launch Filterable DD Modal</button>
    </div>`
})
class FilterableDDWrapperComponent {

  @Input() options: any;
  @Input() optionsLabel: string;
  @Input() dialogTitle: string;
  @Input() inputPlaceholder: string;
  @Input() iconName: string;

  constructor(private matDialog: MatDialog) {
  }

  openFilterableDD(): void {
    const dialogRef = this.matDialog.open(FilterableDropDownComponent, {
      data: {
        options: this.options,
        optionsLabel: this.optionsLabel,
        dialogTitle: this.dialogTitle,
        inputPlaceholder: this.inputPlaceholder,
        iconName: this.iconName
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (isNotNullOrUndefined(result) && result !== '') {
        console.log('Filterable DD Closed with option selected: ', result);
      } else {
        console.log('Filterable DD Closed with no options selected');
      }
    });
  }
}

export const primary = () => ({
  moduleMetadata: {
    declarations: [FilterableDDWrapperComponent],
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })
    ]
  },
  component: FilterableDDWrapperComponent,
  props: {
    options: object('options',
      [{ selectionKey: '0', name: 'Lake Worth' },
        { selectionKey: '1', name: 'Miami' },
        { selectionKey: '2', name: 'Orlando' },
        { selectionKey: '3', name: 'St. Petersburg' },
        { selectionKey: '4', name: 'FLL' },
        { selectionKey: '5', name: 'Hollywood' },
        { selectionKey: '6', name: 'St. Augustine' },
        { selectionKey: '7', name: 'Aventura' },
        { selectionKey: '8', name: 'Margate' }
      ]),
    optionsLabel: text('optionsLabel', 'name'),
    dialogTitle: text('dialogTitle', 'Select Facility'),
    inputPlaceholder: text('inputPlaceholder', 'Filter Facility'),
    iconName: text('iconName', 'search')
  }
});


