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
                loadChildren: () => import('app/modules/home/home.routes'),
            },
            {
                path: 'feature',
                loadChildren: () =>
                    import('app/modules/feature/feature.routes'),
            },
            {
                path: 'orders',
                loadChildren: () =>
                    import('app/modules/feature/feature.routes'),
            },

            // 404 & Catch all
            { path: '**', redirectTo: 'errors/404' },
        ],
    },
];
