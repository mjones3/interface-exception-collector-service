import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { LookUpService } from '@rsa/commons';
import { ORDER_PRODUCT_CATEGORY } from '@rsa/distribution/core/models/orders.model';
import { RETURNS_INSPECTION_STATUS, RETURNS_TRANSIT_TIME_ZONE } from '@rsa/distribution/core/models/returns.models';
import { forkJoin, Observable } from 'rxjs';
import { map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class TransferReceiptResolver implements Resolve<any> {
  constructor(private lookUpService: LookUpService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> | Promise<any> | any {
    // TODO change lookup constants to transfer receipt
    return forkJoin({
      lookups: this.lookUpService
        .getLookUpDataByTypes([RETURNS_TRANSIT_TIME_ZONE, RETURNS_INSPECTION_STATUS, ORDER_PRODUCT_CATEGORY])
        .pipe(map(reasonRes => reasonRes?.body)),
    });
  }
}
