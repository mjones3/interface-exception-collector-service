import { Route } from '@angular/router';
import { initialDataResolver } from 'app/app.resolvers';
import { LayoutComponent } from 'app/layout/layout.component';
import { AuthGuard } from './core/guards/auth.guard';

export const appRoutes: Route[] = [
    // Redirect empty path to '/home'
    { path: '', pathMatch: 'full', redirectTo: 'home' },
    { path: 'signed-in-redirect', pathMatch: 'full', redirectTo: 'home' },

    {
        path: '',
        component: LayoutComponent,
        canActivate: [AuthGuard],
        resolve: {
            initialData: initialDataResolver,
        },
        children: [
            {
                path: 'home',
                canActivate: [AuthGuard],
                loadChildren: () => import('app/modules/home/home.routes'),
            },
            {
                path: 'orders',
                canActivate: [AuthGuard],
                loadChildren: () => import('app/modules/orders/order.routes'),
            },
            {
                path: 'shipment',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import('app/modules/shipments/shipment.routes'),
            },
            {
                path: 'external-transfer',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import(
                        'app/modules/external-transfer/external-transfer.routes'
                    ),
            },
            {
                path: 'recovered-plasma',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import(
                        'app/modules/recovered-plasma-shipment/recovered-plasma-shipment.routes'
                    ),
            },
            {
                path: 'imports',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import('./modules/imports/receiving.routes'),
            },
            {
                path: 'transfer-receipt',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import('./modules/transfer-receipt/transfer-receipt.routes'),
            },

            {
                path: 'irradiation',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import('app/modules/irradiation/irradiation.routes'),
            },

            // 404 & Catch all
            { path: '**', redirectTo: 'errors/404' },
        ],
    },
];
