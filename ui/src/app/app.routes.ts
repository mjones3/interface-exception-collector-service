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
                loadChildren: () =>
                    import(
                        'app/modules/orders/order/search-orders/search-orders.routes'
                    ),
            },
            {
                path: 'orders',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import(
                        'app/modules/orders/order/order-details/order-details.routes'
                    ),
            },
            {
                path: 'shipment',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import('app/modules/shipments/shipment.routes'),
            },
            {
                path: 'shipment',
                canActivate: [AuthGuard],
                loadChildren: () =>
                    import(
                        'app/modules/shipments/fill-products/fill-products.routes'
                    ),
            },

            // 404 & Catch all
            { path: '**', redirectTo: 'errors/404' },
        ],
    },
];
