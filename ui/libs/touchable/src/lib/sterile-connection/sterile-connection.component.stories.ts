import { Component, Input, ViewChild } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { MaterialModule } from '@rsa/material';
import { array, object } from '@storybook/addon-knobs';
import {
  scdSerialNumberMock,
  scdWaferLotNumberMock,
  sterileConnectionProcessMock,
  transferContainerLotNumberMock
} from '../../shared/testing/mocks/data/shared.mock';
import { TouchableComponentsModule } from '../touchable-components.module';
import { SterileConnectionComponent } from './sterile-connection.component';

export default {
  title: 'rsa-sterile-connection'
};

// Component Wrapper
@Component({
  template: `
    <!-- Buttons will be placed in the parent Component as part of the process where the Sterille Connection is been used -->
    <div class="flex justify-end m-4">
      <button class="px-8 mr-2" mat-stroked-button (click)="cancel()" [color]="'accent'" type="button">
        <mat-icon class="icon-size-20" [svgIcon]="'hi_outline:x'"></mat-icon>
        <span class="ml-2">Cancel</span>
      </button>
      <button class="px-8 mr-2" mat-stroked-button (click)="submit()" [color]="'accent'" type="button">
        <mat-icon class="icon-size-20" [svgIcon]="'check'"></mat-icon>
        <span class="ml-2">Submit</span>
      </button>
    </div>

    <rsa-sterile-connection [sterileConnectionCompleted]="sterileConnectionCompleted"
                            [sterileConnectionProcess]="sterileConnectionProcess"
                            [scdSerialNumber]="scdSerialNumber"
                            [scdWaferLotNumber]="scdWaferLotNumber"
                            [transferContainerLotNumber]="transferContainerLotNumber"
                            [satisfactoryFields]="satisfactoryFields">
      <div class="m-8 mb-8">
        <p class="font-bold"> Sterile Connection Information:</p>
        <p class="w-full">Unit Number : Wxxxxxxxxxxxxxx</p>
        <p class="w-full">Product Type: Apheresis Plasma Parent</p>
      </div>
    </rsa-sterile-connection>
  `
})
class SterileConnectionWrapperComponent {

  @Input() sterileConnectionProcess: any;
  @Input() scdSerialNumber: any;
  @Input() scdWaferLotNumber: any;
  @Input() transferContainerLotNumber: any;
  @Input() taskFieldIds: [];
  @Input() satelliteBagFieldIds: [];
  @Input() dialogData: object;
  @Input() satisfactoryFields: object;


  @ViewChild(SterileConnectionComponent) sterileConnectionComponent: SterileConnectionComponent;

  sterileConnectionCompleted: boolean;

  constructor() {
  }

  submit(): void {
    //If sterile connection has been saved into DB then go to next step
    //Decide the option to show the fields based upon sterile connection process selected value
    //If multiple fields are required then identify them from configuration and focus on one field according to the first field shown
    this.sterileConnectionCompleted = true;
  }

  cancel(): void {
    console.log('Cancel Process');
  }
}

export const primary = () => ({
  moduleMetadata: {
    declarations: [SterileConnectionWrapperComponent],
    imports: [
      BrowserAnimationsModule,
      TouchableComponentsModule,
      MaterialModule,
      RouterModule.forRoot([], { useHash: true }),
      TranslateModule.forRoot({
        loader: { provide: TranslateLoader, useClass: TranslateFakeLoader }
      })
    ]
  },
  component: SterileConnectionWrapperComponent,
  props: {
    sterileConnectionProcess: object<any>('sterileConnectionProcess', sterileConnectionProcessMock),
    scdSerialNumber: object<any>('scdSerialNumber', scdSerialNumberMock),
    scdWaferLotNumber: object<any>('scdWaferLotNumber', scdWaferLotNumberMock),
    transferContainerLotNumber: object<any>('transferContainerLotNumber', transferContainerLotNumberMock),
    productsToSelect: array('productsToSelect', ['Apheresis Plasma Parent', 'Apheresis Plasma BagA', 'Apheresis Plasma BagB', 'Apheresis Plasma BagC', 'Apheresis Plasma BagD']),
    taskFieldIds: array('taskFieldIds', ['t1', 't2']),
    satelliteBagFieldIds: array('satelliteBagFieldIds', ['sb1', 'sb2']),
    satisfactoryFields: object('satisfactoryFields', [
      { descriptionKey: '1', formControl: '1' },
      { descriptionKey: '2', formControl: '2' },
      { descriptionKey: '3', formControl: '4' },
      { descriptionKey: '4', formControl: '5' },
      { descriptionKey: '5', formControl: '3' }])
  }
});
