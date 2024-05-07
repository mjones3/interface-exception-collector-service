import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { TranslateModule } from '@ngx-translate/core';
import { EmptyLayoutComponent } from './empty-layout.component';

@NgModule({
  declarations: [EmptyLayoutComponent],
  imports: [CommonModule, RouterModule, TranslateModule],
})
export class EmptyLayoutModule {}
