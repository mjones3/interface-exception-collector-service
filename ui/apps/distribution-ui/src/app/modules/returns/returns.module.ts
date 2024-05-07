import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { TransitTimeModule } from '@rsa/distribution/shared/components/transit-time/transit-time.module';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { ButtonModule } from 'primeng/button';
import { TableModule } from 'primeng/table';
import { ReturnsRoutingModule } from './returns-routing.module';
import { ReturnsComponent } from './returns.component';

@NgModule({
  declarations: [ReturnsComponent],
  imports: [SharedModule, TableModule, ButtonModule, CommonModule, TransitTimeModule, ReturnsRoutingModule],
})
export class ReturnsModule {}
