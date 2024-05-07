import { NgModule } from '@angular/core';
import { Route, RouterModule } from '@angular/router';
import { CreateOrderComponent } from '@rsa/distribution/modules/orders/create-order/create-order.component';
import { FillOrderComponent } from '@rsa/distribution/modules/orders/fill-order/fill-order.component';
import { OrderDetailsComponent } from '@rsa/distribution/modules/orders/order-details/order-details.component';
import { SearchOrdersComponent } from '@rsa/distribution/modules/orders/search-orders/search-orders.component';
import { ValidateOrderComponent } from '@rsa/distribution/modules/orders/validate-order/validate-order.component';
import { ValidateOrderResolver } from '@rsa/distribution/modules/orders/validate-order/validate-order.resolver';
import { EmptyLayoutComponent } from '@rsa/theme';
import { CreateOrderResolver } from './create-order/create-order.resolver';
import { OrderDetailsResolver } from './order-details/order-details.resolver';
import { SearchOrdersResolver } from './search-orders/search-orders.resolver';

export const routes: Route[] = [
  {
    path: '',
    component: EmptyLayoutComponent,
    data: {
      title: 'orders.label',
    },
    children: [
      {
        path: 'search',
        component: SearchOrdersComponent,
        resolve: { seachOrderConfigData: SearchOrdersResolver },
        data: {
          subTitle: 'search-orders.label',
        },
      },
      {
        path: 'create',
        component: CreateOrderComponent,
        resolve: { createOrderConfigData: CreateOrderResolver },
        data: {
          subTitle: 'create-order.label',
        },
      },
      {
        path: ':id/edit',
        component: CreateOrderComponent,
        resolve: { createOrderConfigData: CreateOrderResolver },
        data: {
          subTitle: 'edit-order.label',
        },
      },
      {
        path: ':id/details',
        component: OrderDetailsComponent,
        resolve: { orderDetailsConfigData: OrderDetailsResolver },
        data: {
          subTitle: 'order-details.label',
        },
      },
      {
        path: ':id/fill/:productId',
        component: FillOrderComponent,
        data: {
          subTitle: 'fill-order.label',
        },
      },
      {
        path: ':id/validate',
        component: ValidateOrderComponent,
        resolve: { validateData: ValidateOrderResolver },
        data: {
          subTitle: 'validate-order.label',
        },
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forChild(routes)],
  exports: [RouterModule],
})
export class OrdersRoutingModule {}
