import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import {
  LookUpDto,
  LookUpService,
  OrderBloodTypeDto,
  OrderProductAttributeDto,
  OrderService,
  ProcessProductDto,
  ProcessService,
  ReasonDto,
  ReasonService,
} from '@rsa/commons';
import {
  CANCEL_REASON_ID,
  CLOSE_REASON_ID,
  ORDER_DELIVERY_TYPE,
  ORDER_PROCESS_UUID,
  ORDER_PRODUCT_CATEGORY,
  ORDER_SERVICE_FEE,
  ORDER_SHIPPING_METHOD,
  ORDER_STATUS,
} from '@rsa/distribution/core/models/orders.model';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class OrderDetailsResolver implements Resolve<any> {
  constructor(
    private lookUpService: LookUpService,
    private orderService: OrderService,
    private reasonService: ReasonService,
    private processService: ProcessService
  ) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> | Promise<any> | any {
    return forkJoin([
      this.lookUpService
        .getLookUpDataByTypes([
          ORDER_STATUS,
          ORDER_DELIVERY_TYPE,
          ORDER_SHIPPING_METHOD,
          ORDER_PRODUCT_CATEGORY,
          ORDER_SERVICE_FEE,
        ])
        .pipe(catchError(() => of({ body: [] } as HttpResponse<LookUpDto[]>))),
      this.orderService
        .getOrderBloodTypeByCriteria()
        .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderBloodTypeDto[]>))),
      this.orderService
        .getOrderProductAttributesByCriteria()
        .pipe(catchError(() => of({ body: [] } as HttpResponse<OrderProductAttributeDto[]>))),
      this.reasonService
        .getReasonsByCriteria({ 'reasonType.in': [CANCEL_REASON_ID, CLOSE_REASON_ID].join(',') })
        .pipe(catchError(() => of({ body: [] } as HttpResponse<ReasonDto[]>))),
      this.processService
        .getProcessConfiguration(ORDER_PROCESS_UUID)
        .pipe(catchError(() => of({ body: {} } as HttpResponse<ProcessProductDto>))),
    ]);
  }
}
