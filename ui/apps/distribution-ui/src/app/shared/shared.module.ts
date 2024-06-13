import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { ProductSelectionModule } from '@rsa/distribution/shared/components/product-selection/product-selection.module';
import { MaterialModule } from '@rsa/material';
import { ThemeModule } from '@rsa/theme';
import { TouchableComponentsModule } from '@rsa/touchable';
import { TreoCardModule } from '@treo';
import { ScanUnitNumberCheckDigitComponent } from './components/scan-unit-number-check-digit/scan-unit-number-check-digit.component';

const SHARED_COMMON_MODULES = [
  RsaCommonsModule,
  FormsModule,
  ReactiveFormsModule,
  RouterModule,
  TranslateModule,
  TreoCardModule,
  MaterialModule,
  ThemeModule,
  ProductSelectionModule,
  TouchableComponentsModule,
];

@NgModule({
  declarations: [ScanUnitNumberCheckDigitComponent],
  imports: [
    MaterialModule,
    ReactiveFormsModule,
    TreoCardModule,
    TranslateModule,
    RsaCommonsModule,
    RouterModule,
    TouchableComponentsModule,
  ],
  exports: [...SHARED_COMMON_MODULES, ScanUnitNumberCheckDigitComponent],
})
export class SharedModule {}
