import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { OrderWidgetsSidebarModule } from '@rsa/distribution/shared/components/order-widgets-sidebar/order-widgets-sidebar.module';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { ButtonModule } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';
import { RippleModule } from 'primeng/ripple';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { ShipmentDetailsComponent } from './shipment-details/shipment-details.component';
import { ShipmentRoutingModule } from './shipment-routing.module';

@NgModule({
  declarations: [ShipmentDetailsComponent],
  imports: [
    CommonModule,
    ShipmentRoutingModule,
    SharedModule,
    TableModule,
    ProgressBarModule,
    ButtonModule,
    RippleModule,
    MatButtonToggleModule,
    SelectButtonModule,
    MatProgressBarModule,
    OrderWidgetsSidebarModule,
  ],
})
export class ShipmentModule {}
