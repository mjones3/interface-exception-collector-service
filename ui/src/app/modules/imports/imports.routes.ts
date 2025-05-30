import { Routes } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import {
    EnterShipmentInformationComponent
} from './components/enter-shipment-information/enter-shipment-information.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: { title: 'Imports' },
        resolve: { initialData: initialDataResolver },
        children: [
            {
                path: 'enter-shipment-information',
                component: EnterShipmentInformationComponent,
                data: {
                    subTitle: 'Shipment Information',
                },
            },
        ],
    },
] as Routes;
