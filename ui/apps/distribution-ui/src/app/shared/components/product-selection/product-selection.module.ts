import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { ProductSelectionComponent } from './product-selection.component';

@NgModule({
  declarations: [ProductSelectionComponent],
  imports: [RsaCommonsModule, MaterialModule, RouterModule, TranslateModule],
  exports: [ProductSelectionComponent],
})
export class ProductSelectionModule {}
