import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { ProductSelectionModule } from '@rsa/distribution/shared/components/product-selection/product-selection.module';
import { MaterialModule } from '@rsa/material';
import { ThemeModule } from '@rsa/theme';
import { TreoCardModule } from '@treo';

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
];

@NgModule({
  declarations: [],
  imports: [MaterialModule, ReactiveFormsModule, TreoCardModule, TranslateModule, RsaCommonsModule, RouterModule],
  exports: [...SHARED_COMMON_MODULES],
})
export class SharedModule {}
