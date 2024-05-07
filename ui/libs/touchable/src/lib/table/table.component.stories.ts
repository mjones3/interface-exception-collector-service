import { Component, Input } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { object } from '@storybook/addon-knobs';
import { quarantinesMock } from '../../shared/testing/mocks/data/shared.mock';
import { TouchableComponentsModule } from '../touchable-components.module';

export default {
  title: 'rsa-table'
};

@Component({
  template: `
    <div class="flex justify-center h-full w-full">
      <rsa-table class="w-full" [columns]="columns" [tableConfiguration]="tableConfiguration" [dataSource]="dataSource"
                 [templateRef]="columnTemplateRef" (elementDeleted)="delete($event)">
        <ng-template #columnTemplateRef let-element='element'>
          <div class="p-4">
            <p>{{element.quarantineReason}}</p>
            <p>{{element.quarantineDescription}}</p>
          </div>
        </ng-template>
      </rsa-table>
    </div>`
})
class TableWrapperComponent {

  @Input() columns: any;
  @Input() tableConfiguration;
  @Input() dataSource: any[];
  @Input() totalElements: number;

  constructor() {
  }

  delete(element): void {
    const index = this.dataSource.findIndex(item => element.id === item.id);
    this.dataSource.splice(index, 1);
    this.dataSource = JSON.parse(JSON.stringify(this.dataSource));
  }
}

export const primary = () => ({
  moduleMetadata: {
    declarations: [TableWrapperComponent],
    imports: [BrowserAnimationsModule, TouchableComponentsModule, MaterialModule, RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })]
  },
  component: TableWrapperComponent,
  props: {
    columns: object('columns',
      [{ columnId: 'product', columnHeader: 'Product' },
        { columnId: 'quarantineReason', columnHeader: 'Quarantine Reason' },
        { columnId: 'quarantineDate', columnHeader: 'Quarantine Date' },
        { columnId: 'userId', columnHeader: 'User Id' }
      ]
    ),
    tableConfiguration: object('tableConfiguration', {
      pageSize: [5, 10],
      showDeleteBtn: true,
      expandableRows: true,
      expandableKey: 'quarantineReason'
    }),
    totalElements: quarantinesMock.length,
    dataSource: object('dataSource', quarantinesMock)
  }
});


