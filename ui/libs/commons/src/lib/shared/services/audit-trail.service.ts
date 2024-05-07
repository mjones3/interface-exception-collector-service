import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AuditTrailDto } from '../models/audit-trail.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<AuditTrailDto>;
type EntityArrayResponseType = HttpResponse<AuditTrailDto[]>;

@Injectable({
  providedIn: 'root',
})
export class AuditTrailService {
  auditTrailEndpoint: string;
  createAuditTrailEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.auditTrailEndpoint = config.env.serverApiURL + '/v1/audits';
    this.createAuditTrailEndpoint = this.auditTrailEndpoint;
  }

  public getAuditTrails(pageable, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<AuditTrailDto[]>(this.auditTrailEndpoint, { params: { ...pageable, ...sortInfo }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  /**
   * Get Audit Trails based on Criteria
   */
  public getAuditTrailsByCriteria(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<AuditTrailDto[]>(this.auditTrailEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createAuditTrail(dto: AuditTrailDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<AuditTrailDto>(this.createAuditTrailEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
