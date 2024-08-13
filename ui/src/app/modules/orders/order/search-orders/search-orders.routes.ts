import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { SearchOrdersComponent } from './search-orders.component';
import { initialDataResolver } from '../../../../app.resolvers';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {
            title: 'Shipment',
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
