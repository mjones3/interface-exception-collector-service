import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { OrderDetailsComponent } from './order-details.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Order',
        },
        children: [
            {
                path: ':id/order-details',
                component: OrderDetailsComponent,
                data: {
                    subTitle: 'Order Details',
                },
            },
        ],
    },
] as Routes;
