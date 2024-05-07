import { NgModule } from '@angular/core';
import { Route, RouterModule } from '@angular/router';
import { ImportsResolver } from '@rsa/distribution/modules/imports/imports.resolver';
import { EmptyLayoutComponent } from '@rsa/theme';
import { ImportsComponent } from './imports.component';

export const routes: Route[] = [
  {
    path: '',
    component: EmptyLayoutComponent,
    data: {
      title: 'imports.label',
    },
    children: [
      {
        path: '',
        component: ImportsComponent,
        data: {
          subTitle: 'shipment-information-and-product-selection.label',
        },
        resolve: { importsData: ImportsResolver },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ImportsRoutingModule {}
