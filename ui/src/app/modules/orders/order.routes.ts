import { Routes } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { OrderDetailsComponent } from './order/order-details/order-details.component';
import { SearchOrdersComponent } from './order/search-orders/search-orders.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Order',
        },
        resolve: {
            initialData: initialDataResolver,
        },
        children: [
            {
                path: 'search',
                component: SearchOrdersComponent,
                data: {
                    subTitle: 'Search Orders',
                },
            },
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
