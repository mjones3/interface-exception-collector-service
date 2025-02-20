import { Routes } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { ExternalTransfersComponent } from './components/external-transfers/external-transfers.component';

export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: { title: 'External Transfer' },
        resolve: { initialData: initialDataResolver },
        children: [
            {
                path: '',
                component: ExternalTransfersComponent,
                data: {
                    subTitle: 'Product Selection and Transfer Information',
                },
            },
        ],
    },
] as Routes;
