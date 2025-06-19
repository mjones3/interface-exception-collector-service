import { Routes } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import {
    ImportsEnterShipmentInformationComponent
} from './components/imports-enter-shipment-information/imports-enter-shipment-information.component';
import { EnterProductInformationComponent } from './components/enter-product-information/enter-product-information.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: { title: 'Imports' },
        resolve: { initialData: initialDataResolver },
        children: [
            {
                path: 'imports-enter-shipment-information',
                component: ImportsEnterShipmentInformationComponent,
                data: {
                    subTitle: 'Shipment Information',
                },
            },
            {
                path: ':id/product-information',
                component: EnterProductInformationComponent,
                data: {
                    subTitle: 'Product Information'
                },
            }
        ],
    },
] as Routes;
