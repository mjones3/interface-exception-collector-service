import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { EmptyLayoutComponent } from '@rsa/theme';
import { ShipmentDetailsComponent } from '../shipment/shipment-details/shipment-details.component';
import { FillProductsComponent } from './fill-products/fill-products.component';
import { ShipmentDetailsResolver } from './shipment-details/shipment-details.resolver';

const routes: Routes = [
  {
    path: '',
    component: EmptyLayoutComponent,
    data: {
      title: 'shipment.label',
    },
    children: [
      {
        path: ':id/shipment-details',
        component: ShipmentDetailsComponent,
        resolve: { shipmentDetailsConfigData: ShipmentDetailsResolver },
        data: {
          subTitle: 'shipment-details.label',
        },
      },
      {
        path: ':id/fill-products/:productId',
        component: FillProductsComponent,
        resolve: { shipmentDetailsConfigData: ShipmentDetailsResolver },
        data: {
          subTitle: 'fill-products.label',
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ShipmentRoutingModule {}
