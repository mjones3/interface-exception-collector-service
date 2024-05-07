import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TreoCardComponent } from './card.component';

@NgModule({
  declarations: [
    TreoCardComponent
  ],
  imports: [
    CommonModule
  ],
  exports: [
    TreoCardComponent
  ]
})
export class TreoCardModule {
}
