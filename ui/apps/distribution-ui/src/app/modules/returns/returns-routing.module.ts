import { NgModule } from '@angular/core';
import { Route, RouterModule } from '@angular/router';
import { EmptyLayoutComponent } from '@rsa/theme';
import { ReturnsComponent } from './returns.component';
import { ReturnResolver } from './returns.resolver';

export const routes: Route[] = [
  {
    path: '',
    component: EmptyLayoutComponent,
    data: {
      title: 'returns.label',
    },
    children: [
      {
        path: '',
        component: ReturnsComponent,
        data: {
          subTitle: 'return-information-and-product-selection.label',
        },
        resolve: { returnData: ReturnResolver },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ReturnsRoutingModule {}
