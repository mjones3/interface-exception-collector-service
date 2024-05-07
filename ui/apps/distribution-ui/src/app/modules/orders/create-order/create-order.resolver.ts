import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import {
  LookUpDto,
  LookUpService,
  OrderBloodTypeDto,
  OrderDto,
  OrderProductAttributeDto,
  OrderService,
  ProcessProductDto,
  ProcessService,
} from '@rsa/commons';
import {
  ORDER_DELIVERY_TYPE,
  ORDER_LABEL_STATUS,
  ORDER_PROCESS_UUID,
  ORDER_PRODUCT_CATEGORY,
  ORDER_SERVICE_FEE,
  ORDER_SHIPMENT_TYPE,
  ORDER_SHIPPING_METHOD,
  ORDER_STATUS,
} from '@rsa/distribution/core/models/orders.model';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

const CANCEL_REASON_ID = 11;

@Injectable({
  providedIn: 'root',
})
export class CreateOrderResolver implements Resolve<any> {
  constructor(
    private lookUpService: LookUpService,
    private orderService: OrderService,
    private processService: ProcessService
  ) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> | Promise<any> | any {
    const orderId = route.params.id;

    const sub$: Observable<
      | HttpResponse<LookUpDto[]>
      | HttpResponse<OrderProductAttributeDto[]>
      | HttpResponse<ProcessProductDto>
      | HttpResponse<OrderBloodTypeDto[]>
      | HttpResponse<OrderDto>
      | HttpResponse<{ orderNumber: string }>
    >[] = [
      this.lookUpService
        .getLookUpDataByTypes([
          ORDER_SHIPPING_METHOD,
          ORDER_SHIPMENT_TYPE,
          ORDER_DELIVERY_TYPE,
          ORDER_PRODUCT_CATEGORY,
          ORDER_SERVICE_FEE,
          ORDER_STATUS,
          ORDER_LABEL_STATUS,
        ])
        .pipe(catchError(() => of({ body: [] } as HttpResponse<LookUpDto[]>))),
      this.orderService
        .getOrderProductAttributesByCriteria()
        .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderProductAttributeDto[]>))),
      this.processService
        .getProcessConfiguration(ORDER_PROCESS_UUID)
        .pipe(catchError(() => of({ body: {} } as HttpResponse<ProcessProductDto>))),
    ];

    if (orderId) {
      sub$.push(
        ...[
          this.orderService
            .getOrderBloodTypeByCriteria()
            .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderBloodTypeDto[]>))),
          this.orderService.getOrderById(orderId).pipe(catchError(() => of({ body: {} } as HttpResponse<OrderDto>))),
        ]
      );
    } else {
      sub$.push(
        this.orderService
          .getNextOrderNumber()
          .pipe(catchError(() => of({ body: { orderNumber: '' } } as HttpResponse<{ orderNumber: string }>)))
      );
    }

    return forkJoin(sub$);
  }
}
