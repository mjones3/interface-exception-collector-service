import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { LookUpService, ReasonService } from '@rsa/commons';
import { ORDER_PRODUCT_CATEGORY } from '@rsa/distribution/core/models/orders.model';
import {
  RETURNS_INSPECTION_STATUS,
  RETURNS_TRANSIT_TIME_ZONE,
  RETURN_REASON_TYPE_KEY,
} from '@rsa/distribution/core/models/returns.models';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ReturnResolver implements Resolve<any> {
  constructor(private reasonService: ReasonService, private lookUpService: LookUpService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> | Promise<any> | any {
    return forkJoin({
      reason: this.reasonService.getReasonsByCriteria({ 'reasonTypeKey.equals': RETURN_REASON_TYPE_KEY }).pipe(
        catchError(this.emptyBody),
        map(reasonRes => reasonRes.body)
      ),
      lookups: this.lookUpService
        .getLookUpDataByTypes([RETURNS_TRANSIT_TIME_ZONE, RETURNS_INSPECTION_STATUS, ORDER_PRODUCT_CATEGORY])
        .pipe(
          catchError(this.emptyBody),
          map(reasonRes => reasonRes.body)
        ),
    });
  }

  private emptyBody() {
    return of({ body: [] });
  }
}
