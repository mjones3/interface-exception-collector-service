import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { ShipmentDetailsComponent } from './shipment-details/shipment-details.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Shipment'
        },
        children: [
            {
              path: ':id/shipment-details',
              component: ShipmentDetailsComponent,
              data: {
                subTitle: 'Shipment Details',
              },
            },
        ]

    },
] as Routes;
