import { Routes } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { RecoveredPlasmaShippingDetailsComponent } from './components/recovered-plasma-shipping-details/recovered-plasma-shipping-details.component';
import { SearchShipmentComponent } from './components/search-shipment/search-shipment.component';

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
                path: 'shipment-details',
                component: RecoveredPlasmaShippingDetailsComponent,
                data: {
                    subTitle: 'Shipment Details',
                },
            },
        ],
    },
] as Routes;
