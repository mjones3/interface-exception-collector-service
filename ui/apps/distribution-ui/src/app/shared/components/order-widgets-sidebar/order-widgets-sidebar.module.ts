import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { RsaCommonsModule } from '@rsa/commons';
import { MaterialModule } from '@rsa/material';
import { OrderWidgetsSidebarComponent } from './order-widgets-sidebar.component';

@NgModule({
  declarations: [OrderWidgetsSidebarComponent],
  imports: [RsaCommonsModule, MaterialModule, RouterModule, TranslateModule],
  exports: [OrderWidgetsSidebarComponent],
})
export class OrderWidgetsSidebarModule {}
