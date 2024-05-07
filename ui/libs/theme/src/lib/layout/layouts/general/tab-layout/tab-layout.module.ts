import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatTabsModule } from '@angular/material/tabs';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { TabLayoutComponent } from './tab-layout.component';

@NgModule({
  declarations: [TabLayoutComponent],
  imports: [CommonModule, MatTabsModule, RouterModule, TranslateModule],
})
export class TabLayoutModule {}
