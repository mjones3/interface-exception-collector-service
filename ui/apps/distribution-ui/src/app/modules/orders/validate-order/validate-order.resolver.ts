import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { CustomerService, LookUpService, OrderService, ReasonService, ShipmentService } from '@rsa/commons';
import {
  ORDER_DELIVERY_TYPE,
  ORDER_PRODUCT_CATEGORY,
  ORDER_SERVICE_FEE,
  ORDER_SHIPPING_METHOD,
  ORDER_STATUS,
} from '@rsa/distribution/core/models/orders.model';
import { forkJoin, Observable, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ValidateOrderResolver implements Resolve<any> {
  constructor(
    private orderService: OrderService,
    private customerService: CustomerService,
    private lookupService: LookUpService,
    private reasonService: ReasonService,
    private shipmentService: ShipmentService
  ) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> | Promise<any> | any {
    const criteria = {
      orderId: route.params.id,
      withEmployeeId: true,
      filled: true,
      size: 100,
    };
    return this.orderService.getOrderById(route.params.id).pipe(
      switchMap(response => {
        if (response.body) {
          const order = this.getBody(response);
          return forkJoin({
            lookups: this.lookupService
              .getLookUpDataByTypes([
                ORDER_STATUS,
                ORDER_DELIVERY_TYPE,
                ORDER_SHIPPING_METHOD,
                ORDER_PRODUCT_CATEGORY,
                ORDER_SERVICE_FEE,
              ])
              .pipe(map(this.getBody)),
            billingCustomer:
              order.billingCustomerId != null
                ? this.customerService.getCustomerById(order.billingCustomerId).pipe(map(this.getBody))
                : of(null),
            shippingCustomer:
              order.shippingCustomerId != null
                ? this.customerService.getCustomerById(order.shippingCustomerId).pipe(map(this.getBody))
                : of(null),
            products: this.orderService
              .getOrderItemInventoriesPendingToShipByCriteria(criteria, order.labelStatus === 'UNLABELED')
              .pipe(map(this.getBody)),
            shipments: this.shipmentService.getShipmentByCriteria({ orderId: order.id }).pipe(map(this.getBody)),
            order: of(order),
          });
        }
      })
    );
  }

  private getBody(response: HttpResponse<any>) {
    return response.body;
  }
}
