import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ViewPickListComponent } from '@rsa/distribution/modules/shipment/view-pick-list/view-pick-list.component';
import { OrderWidgetsSidebarModule } from '@rsa/distribution/shared/components/order-widgets-sidebar/order-widgets-sidebar.module';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { ButtonModule } from 'primeng/button';
import { ProgressBarModule } from 'primeng/progressbar';
import { RippleModule } from 'primeng/ripple';
import { SelectButtonModule } from 'primeng/selectbutton';
import { TableModule } from 'primeng/table';
import { EnterUnitNumberProductCodeComponent } from './enter-unit-number-product-code/enter-unit-number-product-code.component';
import { FillProductsComponent } from './fill-products/fill-products.component';
import { ShipmentDetailsComponent } from './shipment-details/shipment-details.component';
import { ShipmentRoutingModule } from './shipment-routing.module';
import { ViewPackingListComponent } from './view-packing-list/view-packing-list.component';
import { ViewShippingLabelComponent } from './view-shipping-label/view-shipping-label.component';

@NgModule({
  declarations: [
    ShipmentDetailsComponent,
    ViewPickListComponent,
    FillProductsComponent,
    EnterUnitNumberProductCodeComponent,  
    ViewPackingListComponent,
    ViewShippingLabelComponent,
  ],
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
