import { Component, ViewChild } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterModule } from '@angular/router';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import { EnvironmentConfigService } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { action } from '@storybook/addon-actions';
import { text } from '@storybook/addon-knobs';
import { moduleMetadata } from '@storybook/angular';
import { TreoModule } from '@treo';
import { envConfigFactoryMock } from '../../shared/testing/mocks/env-config.mock';
import { TouchableComponentsModule } from '../touchable-components.module';
import { VolumeComponent } from './volume.component';

@Component({
  template: `
    <div class="process-content flex-1 px-8">
      <!-- Top Action Buttons -->
      <div class="w-full action-header-buttons flex my-4 justify-end">
        <button mat-raised-button (click)="onSubmit()">
          <span class="ml-2">Submit</span>
        </button>
      </div>
      <rsa-volume
        [headerTitle]="headerTitle"
        [subTitleInstructions]="subTitleInstructions"
        (calculateVolumeFormSubmitted)="calculateVolume($event)"
      >
        <treo-card>
          <div class="w-full py-4">
            <mat-card-header class="flex pb-2 px-4">
              <mat-card-title>Donation Information</mat-card-title>
            </mat-card-header>
            <mat-divider></mat-divider>
            <mat-card-content class="px-4">
              <div class="flex flex-col bg-white-200">
                <div class="text-gray-700 text-left pl-4 pt-4"><strong>Unit Number:</strong> Wxxxxxxxxxxxx</div>
                <div class="text-gray-700 text-left pl-4"><strong>Blood Type:</strong> O Positive</div>
                <div class="text-gray-700 text-left pl-4"><strong>Collection Date:</strong> 07/24/2020</div>
                <div class="text-gray-700 text-left pl-4"><strong>Draw Time:</strong> 07/24/2020 14:15 EDT</div>
              </div>
            </mat-card-content>
          </div>
        </treo-card>
      </rsa-volume>
    </div>
  `,
})
class WrappedComponent {
  headerTitle = 'Calculate Volume';
  subTitleInstructions = 'Place the plasma unit on the scale and touch the Calculate Volume button.';
  enableWeightControl = true;
  group = new FormGroup({
    volume: new FormControl(''),
  });
  @ViewChild(VolumeComponent, { static: false }) volumeComponent: VolumeComponent;

  onSubmit() {
    console.log('this.group.value', this.volumeComponent.form);
  }

  calculateVolume($event: any) {
    // Change the volume with data come from api
    const mockWeight = 250;
    this.volumeComponent.setVolumeFromWeight(mockWeight);
  }
}

export default {
  title: 'rsa-volume',
  decorators: [
    moduleMetadata({
      declarations: [WrappedComponent],
      imports: [
        BrowserAnimationsModule,
        TouchableComponentsModule,
        TreoModule,
        MaterialModule,
        RouterModule.forRoot([], { useHash: true }),
        TranslateModule.forRoot({
          loader: { provide: TranslateLoader, useClass: TranslateFakeLoader },
        }),
      ],
      providers: [
        {
          provide: EnvironmentConfigService,
          useFactory: envConfigFactoryMock,
        },
      ],
    }),
  ],
};

export const actionsData = {
  calculateVolumeButtonClicked: action('calculateVolumeButtonClicked'),
  calculateVolumeFormSubmitted: action('calculateVolumeFormSubmitted'),
};

export const primary = () => ({
  component: VolumeComponent,
  props: {
    headerTitle: text('headerTitle', 'Calculate Volume'),
    subTitleInstructions: text(
      'subTitleInstructions',
      'Place the plasma unit on the scale and touch the Calculate Volume button.'
    ),
    calculateVolumeFormSubmitted: actionsData.calculateVolumeFormSubmitted,
  },
});

export const disabledVolume = () => ({
  component: VolumeComponent,
  props: {
    headerTitle: text('headerTitle', 'Calculate Volume'),
    subTitleInstructions: text(
      'subTitleInstructions',
      'Place the plasma unit on the scale and touch the Calculate Volume button.'
    ),
    calculateVolumeFormSubmitted: actionsData.calculateVolumeFormSubmitted,
    disabledVolume: true,
  },
});

export const wrapped = () => ({
  component: WrappedComponent,
  props: {
    headerTitle: text('headerTitle', 'Calculate Volume'),
    subTitleInstructions: text(
      'subTitleInstructions',
      'Place the plasma unit on the scale and touch the Calculate Volume button.'
    ),
    calculateVolumeFormSubmitted: actionsData.calculateVolumeFormSubmitted,
  },
});
