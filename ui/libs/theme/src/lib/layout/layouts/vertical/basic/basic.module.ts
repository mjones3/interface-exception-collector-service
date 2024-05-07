import { CommonModule } from '@angular/common';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatMenuModule } from '@angular/material/menu';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TreoNavigationModule } from '@treo';
import { ShortcutsModule } from '../../../shortcuts/shortcuts.module';
import { UserModule } from '../../../user/user.module';
import { BasicLayoutComponent } from './basic.component';

@NgModule({
  declarations: [BasicLayoutComponent],
  imports: [
    HttpClientModule,
    RouterModule,
    MatButtonModule,
    MatDividerModule,
    MatIconModule,
    MatMenuModule,
    CommonModule,
    TranslateModule,
    TreoNavigationModule,
    MatTooltipModule,
    UserModule,
    ShortcutsModule,
  ],
  exports: [BasicLayoutComponent],
})
export class BasicLayoutModule {}
