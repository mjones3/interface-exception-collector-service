import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { LookUpService } from '@rsa/commons';
import {
  IMPORTS_BLOOD_TYPES,
  IMPORTS_INSPECTION_STATUS,
  IMPORTS_TRANSIT_TIME_ZONE,
} from '@rsa/distribution/core/models/imports.models';
import { ORDER_PRODUCT_CATEGORY } from '@rsa/distribution/core/models/orders.model';
import { forkJoin, Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ImportsResolver implements Resolve<any> {
  constructor(private lookUpService: LookUpService) {}

  resolve(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<any> | Promise<any> | any {
    return forkJoin({
      lookups: this.lookUpService
        .getLookUpDataByTypes([
          IMPORTS_TRANSIT_TIME_ZONE,
          IMPORTS_INSPECTION_STATUS,
          ORDER_PRODUCT_CATEGORY,
          IMPORTS_BLOOD_TYPES,
        ])
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
