import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReasonTypeDto } from '../models/reason-type.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ReasonTypeService {
  endpoint: string;
  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.endpoint = config.env && config.env.serverApiURL ? config.env.serverApiURL + '/v1/reason-types' : '';
  }

  /**
   * Get Reason-Type by id
   * @param typeId
   * @param pageable
   */
  getReasonTypeById(id: number): Observable<HttpResponse<ReasonTypeDto[]>> {
    return this.httpClient
      .get<ReasonTypeDto[]>(this.endpoint + '/' + id, {
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  getReasonTypeByCriteria(criteria?: {}): Observable<HttpResponse<ReasonTypeDto[]>> {
    return this.httpClient
      .get<ReasonTypeDto[]>(this.endpoint, {
        params: { ...criteria, size: '1000' },
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  private handleError(err) {
    /**
     * Handle logic here
     **/
    return throwError(err.message);
  }
}
