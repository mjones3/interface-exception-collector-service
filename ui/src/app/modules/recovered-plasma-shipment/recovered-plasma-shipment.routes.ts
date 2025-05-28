import { Routes } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { RecoveredPlasmaShippingDetailsComponent } from './components/recovered-plasma-shipping-details/recovered-plasma-shipping-details.component';
import { SearchShipmentComponent } from './components/search-shipment/search-shipment.component';
import { ManageCartonComponent } from './components/manage-carton-products/manage-carton-products.component';
import { ShipmentCommentsComponent } from './components/shipment-comments/shipment-comments.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: { title: 'Recovered Plasma Shipping' },
        resolve: { initialData: initialDataResolver },
        children: [
            {
                path: '',
                component: SearchShipmentComponent,
                data: {
                    subTitle: 'Search Shipment',
                },
            },
            {
                path: ':id/shipment-details',
                component: RecoveredPlasmaShippingDetailsComponent,
                data: {
                    subTitle: 'Shipment Details',
                },
            },
            {
                path: ':id/carton-details',
                component: ManageCartonComponent,
                data: {
                    subTitle: 'Manage Carton Products',
                },
            },
            {
                path: ':id/shipment-details/comments',
                component: ShipmentCommentsComponent,
                data: {
                    subTitle: 'Verify Products',
                },
            },
        ],
    },
] as Routes;
