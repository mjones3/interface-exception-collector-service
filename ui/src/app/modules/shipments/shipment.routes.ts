import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { SearchShipmentsComponent } from './search-shipments/search-shipments.component';
import { ShipmentDetailsComponent } from './shipment-details/shipment-details.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Shipment',
        },
        children: [
            {
                path: 'search',
                component: SearchShipmentsComponent,
                data: {
                    subTitle: 'Order-Fulfillment',
                },
            },
            {
                path: ':id/shipment-details',
                component: ShipmentDetailsComponent,
                data: {
                    subTitle: 'Shipment Details',
                },
            },
        ],
    },
] as Routes;
