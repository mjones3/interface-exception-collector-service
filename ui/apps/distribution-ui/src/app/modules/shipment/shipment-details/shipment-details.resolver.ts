import { HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, Resolve, RouterStateSnapshot } from '@angular/router';
import { ProcessProductDto, ProcessService } from '@rsa/commons';
import { ORDER_PROCESS_UUID } from '@rsa/distribution/core/models/orders.model';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

@Injectable({
  providedIn: 'root',
})
export class ShipmentDetailsResolver
  implements Resolve<Observable<HttpResponse<ProcessProductDto>> | Promise<HttpResponse<ProcessProductDto>>> {
  constructor(private processService: ProcessService) {}

  resolve(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<HttpResponse<ProcessProductDto>> | Promise<HttpResponse<ProcessProductDto>> {
    return this.processService
      .getProcessConfiguration(ORDER_PROCESS_UUID)
      .pipe(catchError(() => of({ body: {} } as HttpResponse<ProcessProductDto>)));
  }
}
