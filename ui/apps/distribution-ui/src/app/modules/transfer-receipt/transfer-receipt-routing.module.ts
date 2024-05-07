import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { TransferReceiptComponent } from '@rsa/distribution/modules/transfer-receipt/transfer-receipt/transfer-receipt.component';
import { TransferReceiptResolver } from '@rsa/distribution/modules/transfer-receipt/transfer-receipt/transfer-receipt.resolver';
import { EmptyLayoutComponent } from '@rsa/theme';

const routes: Routes = [
  {
    path: '',
    component: EmptyLayoutComponent,
    data: {
      title: 'transfer-receipt.label',
    },
    children: [
      {
        path: '',
        component: TransferReceiptComponent,
        data: {
          subTitle: 'transfer-receipt-prod-selection.label',
        },
        resolve: { transferReceipt: TransferReceiptResolver },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class TransferReceiptRoutingModule {}
