import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { SearchOrdersComponent } from './search-orders.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Shipment'
        },
        children: [
            {
              path: 'search',
              component: SearchOrdersComponent,
              data: {
                subTitle: 'Order-Fulfillment',
              },
            },
        ]

    },
] as Routes;
