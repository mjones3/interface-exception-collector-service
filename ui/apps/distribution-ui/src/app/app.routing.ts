import { Route } from '@angular/router';
import { AuthGuard, InitialDataResolver, ROLE_TRANSFER_RECEIPT_ALL } from '@rsa/commons';
import { EmptyLayoutComponent } from '@rsa/theme';
import { LayoutComponent } from './layout/layout.component';

// @formatter:off
// tslint:disable:max-line-length
export const appRoutes: Route[] = [
  // Redirect empty path to '/home'
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  // Redirect signed in user to the '/home'
  { path: 'signed-in-redirect', pathMatch: 'full', redirectTo: 'home' },

  // Donor routes
  {
    path: '',
    component: LayoutComponent,
    canActivate: [AuthGuard],
    resolve: {
      initialData: InitialDataResolver,
    },
    children: [
      { path: 'home', loadChildren: () => import('./modules/landing/home/home.module').then(m => m.LandingHomeModule) },
      { path: 'orders', loadChildren: () => import('./modules/orders/orders.module').then(m => m.OrdersModule) },
      { path: 'returns', loadChildren: () => import('./modules/returns/returns.module').then(m => m.ReturnsModule) },
      { path: 'imports', loadChildren: () => import('./modules/imports/imports.module').then(m => m.ImportsModule) },
      {
        path: 'external-transfers',
        loadChildren: () =>
          import('./modules/external-transfers/external-transfers.module').then(m => m.ExternalTransfersModule),
      },
      {
        path: 'transfer-receipt',
        loadChildren: () =>
          import('./modules/transfer-receipt/transfer-receipt.module').then(m => m.TransferReceiptModule),
        canActivate: [AuthGuard],
        data: {
          roles: [ROLE_TRANSFER_RECEIPT_ALL],
        },
      },
      // 404 & Catch all
      { path: 'errors', loadChildren: () => import('libs/errors/src/lib/errors.module').then(m => m.ErrorsModule) },
      { path: '**', redirectTo: 'errors/404' },
    ],
  },

  {
    path: '',
    component: EmptyLayoutComponent,
    data: {
      layout: 'empty',
    },
    children: [{ path: 'sign-out', loadChildren: () => import('@rsa/theme').then(m => m.AuthSignOutModule) }],
  },

  // 404 & Catch all
  {
    path: 'errors',
    component: EmptyLayoutComponent,
    data: {
      layout: 'empty',
    },
    loadChildren: () => import('@rsa/errors').then(m => m.ErrorsModule),
  },
  { path: '**', redirectTo: 'errors/404' },
];
