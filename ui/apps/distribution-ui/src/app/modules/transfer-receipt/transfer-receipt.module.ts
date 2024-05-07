import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { TemperatureInputComponent } from '@rsa/distribution/modules/transfer-receipt/temperature-input/temperature-input.component';
import { TransferReceiptProductSelectionComponent } from '@rsa/distribution/modules/transfer-receipt/transfer-receipt-product-selection/transfer-receipt-product-selection.component';
import { TransferReceiptComponent } from '@rsa/distribution/modules/transfer-receipt/transfer-receipt/transfer-receipt.component';
import { ProductSelectionModule } from '@rsa/distribution/shared/components/product-selection/product-selection.module';
import { TransitTimeModule } from '@rsa/distribution/shared/components/transit-time/transit-time.module';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { TreoScrollbarModule } from '@treo';
import { TableModule } from 'primeng/table';
import { TransferReceiptRoutingModule } from './transfer-receipt-routing.module';

@NgModule({
  declarations: [TransferReceiptComponent, TransferReceiptProductSelectionComponent, TemperatureInputComponent],
  imports: [
    SharedModule,
    TableModule,
    TreoScrollbarModule,
    DragDropModule,
    TransferReceiptRoutingModule,
    TransitTimeModule,
    ProductSelectionModule,
  ],
})
export class TransferReceiptModule {}
