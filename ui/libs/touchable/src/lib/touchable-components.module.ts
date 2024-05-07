import { CUSTOM_ELEMENTS_SCHEMA, NgModule } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { HammerModule, HAMMER_GESTURE_CONFIG } from '@angular/platform-browser';
import { TranslateModule } from '@ngx-translate/core';
import { IconService, RsaCommonsModule } from '@rsa/commons';
import { ThemeModule } from '@rsa/theme';
import { TreoCardModule, TreoMessageModule, TreoScrollbarModule } from '@treo';
import 'hammerjs';
import { HammerConfig } from '../shared/hammerjs/hammer.config';
import { COMPONENTS } from './components';
import { ConfirmationDialogComponent } from './confirmation-dialog/confirmation-dialog.component';
import { FilterableDropDownComponent } from './filterable-drop-down/filterable-drop-down.component';
import { DRIP_ICONS, HEROIC_ICONS, RSA_ICONS } from './icons';
import { OnScreenKeyboardComponent } from './on-screen-keyboard/on-screen-keyboard.component';
import { OptionsPickerDialogComponent } from './options-picker-dialog/options-picker-dialog.component';

@NgModule({
  imports: [
    RsaCommonsModule,
    ThemeModule,
    TreoCardModule,
    TreoMessageModule,
    TreoScrollbarModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
    MatButtonToggleModule,
    HammerModule,
    TranslateModule,
  ],
  declarations: [...COMPONENTS],
  schemas: [CUSTOM_ELEMENTS_SCHEMA],
  exports: [...COMPONENTS],
  entryComponents: [
    OptionsPickerDialogComponent,
    ConfirmationDialogComponent,
    FilterableDropDownComponent,
    OnScreenKeyboardComponent,
  ],
  providers: [
    {
      provide: HAMMER_GESTURE_CONFIG,
      useClass: HammerConfig,
    },
    {
      // Use the 'fill' appearance on form fields by default
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        appearance: 'fill',
      },
    },
  ],
})
export class TouchableComponentsModule {
  constructor(private iconService: IconService) {
    // Add RSA icons
    iconService.addIcon(...RSA_ICONS, ...HEROIC_ICONS, ...DRIP_ICONS);
    // TODO remove this when the icons can be imported separately
    iconService.addIconSet('assets/icons/material-outline.svg');
  }
}
