import { DragDropModule } from '@angular/cdk/drag-drop';
import { NgModule } from '@angular/core';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { TreoScrollbarModule } from '@treo';
import { TableModule } from 'primeng/table';
import { AddProductsModalComponent } from './add-products-modal/add-products-modal.component';
import { ExternalTransfersRoutingModule } from './external-transfers-routing.module';
import { ExternalTransfersComponent } from './external-transfers/external-transfers.component';

@NgModule({
  declarations: [ExternalTransfersComponent, AddProductsModalComponent],
  imports: [SharedModule, ExternalTransfersRoutingModule, TableModule, TreoScrollbarModule, DragDropModule],
  providers: [
    { provide: DateAdapter, useClass: MomentDateAdapter, deps: [MAT_DATE_LOCALE] },
    {
      provide: MAT_DATE_FORMATS,
      useValue: {
        parse: {
          dateInput: 'L',
        },
        display: {
          dateInput: 'L',
          monthYearLabel: 'MMM YYYY',
        },
      },
    },
  ],
})
export class ExternalTransfersModule {}
