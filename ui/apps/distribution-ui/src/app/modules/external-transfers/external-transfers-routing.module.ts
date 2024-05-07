import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { ExternalTransfersComponent } from '@rsa/distribution/modules/external-transfers/external-transfers/external-transfers.component';
import { EmptyLayoutComponent } from '@rsa/theme';

const routes: Routes = [
  {
    path: '',
    component: EmptyLayoutComponent,
    data: {
      title: 'external-transfers.label',
    },
    children: [
      {
        path: '',
        component: ExternalTransfersComponent,
        data: {
          subTitle: '',
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class ExternalTransfersRoutingModule {}
