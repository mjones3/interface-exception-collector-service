import { Routes } from '@angular/router';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { FillProductsComponent } from './fill-products/fill-products.component';
import { SearchShipmentsComponent } from './search-shipments/search-shipments.component';
import { ShipmentDetailsComponent } from './shipment-details/shipment-details.component';
import { VerifyProductsComponent } from './verify-products/verify-products.component';

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
            {
                path: ':id/fill-products/:productId',
                component: FillProductsComponent,
                data: {
                    subTitle: 'Fill Products',
                },
            },
            {
                path: ':id/verify-products',
                component: VerifyProductsComponent,
                data: {
                    subTitle: 'Verify Products',
                },
            },
        ],
    },
] as Routes;
