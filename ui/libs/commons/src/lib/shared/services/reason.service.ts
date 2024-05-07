import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DiscardReason, ReasonDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<DiscardReason[]>;

@Injectable({
  providedIn: 'root',
})
export class ReasonService {
  reasonEndpoint: string;
  private resourceUrl: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.resourceUrl = config.env && config.env.serverApiURL ? config.env.serverApiURL + '/v1/discard-reasons' : '';
    this.reasonEndpoint = config.env && config.env.serverApiURL ? config.env.serverApiURL + '/v1/reasons' : '';
  }

  getReasons(): Observable<EntityArrayResponseType> {
    const options = new HttpParams().set('process', 'discard').set('page', '0').set('size', '100');
    return this.httpClient
      .get<DiscardReason[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get Reasons by criteria typeId
   * @param typeId
   * @param pageable
   */
  getReasonsByTypeId(typeId: number): Observable<HttpResponse<ReasonDto[]>> {
    return this.httpClient
      .get<ReasonDto[]>(this.reasonEndpoint + '?reasonType=' + typeId, {
        //params: { sort: 'descriptionKey,ASC' },
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get Reasons by criteria typeId
   * @param typeId
   * @param pageable
   */
  getActiveReasonsByReasonTypeKey(reasonTypeKey: string): Observable<HttpResponse<ReasonDto[]>> {
    return this.httpClient
      .get<ReasonDto[]>(this.reasonEndpoint + '?reasonTypeKey.equals=' + reasonTypeKey + '&active=true', {
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get Reasons by criteria typeId and params
   * @param typeId
   * @param pageable
   */
  getReasonsByTypeIdAndParams(typeId: number, parameters): Observable<HttpResponse<ReasonDto[]>> {
    return this.httpClient
      .get<ReasonDto[]>(this.reasonEndpoint + '?reasonType=' + typeId, {
        params: parameters, // { sort: 'descriptionKey,ASC' }
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  getReasonsByCriteria(criteria?: {}): Observable<HttpResponse<ReasonDto[]>> {
    return this.httpClient
      .get<ReasonDto[]>(this.reasonEndpoint, {
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
