import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { SearchOrdersComponent } from './search-orders.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Order'
        },
        children: [
            {
              path: 'search',
              component: SearchOrdersComponent,
              data: {
                subTitle: 'Search Orders',
              },
            },
        ]

    },
] as Routes;
