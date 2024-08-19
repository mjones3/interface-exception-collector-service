import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { initialDataResolver } from '../../../../app.resolvers';
import { SearchOrdersComponent } from './search-orders.component';

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
        ],
    },
] as Routes;
