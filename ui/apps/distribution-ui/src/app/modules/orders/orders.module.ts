import { CdkStepperModule } from '@angular/cdk/stepper';
import { NgModule } from '@angular/core';
import { MomentDateAdapter } from '@angular/material-moment-adapter';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE } from '@angular/material/core';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatStepperModule } from '@angular/material/stepper';
import { OrderWidgetsSidebarModule } from '@rsa/distribution/shared/components/order-widgets-sidebar/order-widgets-sidebar.module';
import { ProductSelectionModule } from '@rsa/distribution/shared/components/product-selection/product-selection.module';
import { SharedModule } from '@rsa/distribution/shared/shared.module';
import { ButtonModule } from 'primeng/button';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { MultiSelectModule } from 'primeng/multiselect';
import { ProgressBarModule } from 'primeng/progressbar';
import { RippleModule } from 'primeng/ripple';
import { SelectButtonModule } from 'primeng/selectbutton';
import { SliderModule } from 'primeng/slider';
import { TableModule } from 'primeng/table';
import { AddProductModalComponent } from './add-product-modal/add-product-modal.component';
import { CancelOrderModalComponent } from './cancel-order-modal/cancel-order-modal.component';
import { CloseOrderModalComponent } from './close-order-modal/close-order-modal.component';
import { CreateOrderComponent } from './create-order/create-order.component';
import { FillOrderComponent } from './fill-order/fill-order.component';
import { OrderDetailsComponent } from './order-details/order-details.component';
import { OrdersRoutingModule } from './orders-routing.module';
import { SearchOrdersComponent } from './search-orders/search-orders.component';
import { ServiceFeeModalComponent } from './service-fee-modal/service-fee-modal.component';
import { ValidateOrderComponent } from './validate-order/validate-order.component';

@NgModule({
  declarations: [
    SearchOrdersComponent,
    CreateOrderComponent,
    ServiceFeeModalComponent,
    AddProductModalComponent,
    CancelOrderModalComponent,
    OrderDetailsComponent,
    FillOrderComponent,
    ValidateOrderComponent,
    CloseOrderModalComponent,
  ],
  imports: [
    SharedModule,
    TableModule,
    SliderModule,
    DropdownModule,
    MultiSelectModule,
    CdkStepperModule,
    MatStepperModule,
    ProgressBarModule,
    ButtonModule,
    RippleModule,
    CalendarModule,
    OrdersRoutingModule,
    MatButtonToggleModule,
    SelectButtonModule,
    MatProgressBarModule,
    OrderWidgetsSidebarModule,
    MatAutocompleteModule,
    MatGridListModule,
    ProductSelectionModule,
  ],
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
export class OrdersModule {}
