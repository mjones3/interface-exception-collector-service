import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { LookUpDto, LookUpService, ProcessProductDto, ProcessService } from '@rsa/commons';
import {
  ORDER_DELIVERY_TYPE,
  ORDER_PROCESS_UUID,
  ORDER_SHIPMENT_TYPE,
  ORDER_STATUS,
} from '@rsa/distribution/core/models/orders.model';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class SearchOrdersResolver implements Resolve<any> {
  constructor(private lookUpService: LookUpService, private processService: ProcessService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> | Promise<any> | any {
    return forkJoin([
      this.lookUpService
        .getLookUpDataByTypes([ORDER_STATUS, ORDER_DELIVERY_TYPE, ORDER_SHIPMENT_TYPE])
        .pipe(catchError(() => of({ body: [] } as HttpResponse<LookUpDto[]>))),
      this.processService.getProcessConfiguration(ORDER_PROCESS_UUID).pipe(
        catchError(() =>
          of({
            body: {
              properties: new Map<string, string>(),
            },
          } as HttpResponse<ProcessProductDto>)
        )
      ),
    ]);
  }
}
