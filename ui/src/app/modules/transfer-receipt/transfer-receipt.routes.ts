import { Routes } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { EmptyLayoutComponent } from 'app/layout/layouts/empty/empty.component';
import { TransferReceiptComponent } from './components/transfer-receipt/transfer-receipt.component';


export default [
    {
        path: '',
        component: EmptyLayoutComponent,
        data: {title: 'Transfers Receipt'},
        resolve: {initialData: initialDataResolver},
        children: [
            {
                path: '',
                component: TransferReceiptComponent,
                data: {
                    subTitle: 'Transfer Information',
                },
            }
        ],
    },
] as Routes;
