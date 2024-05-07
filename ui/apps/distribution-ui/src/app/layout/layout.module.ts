import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { LayoutComponent } from '@rsa/distribution/layout/layout.component';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { BasicLayoutModule, TabLayoutModule } from '@rsa/theme';

const modules = [BasicLayoutModule, TabLayoutModule, CommonModule];

@NgModule({
  declarations: [LayoutComponent],
  imports: [SharedModule, ...modules],
  exports: [...modules, LayoutComponent],
})
export class LayoutModule {}
