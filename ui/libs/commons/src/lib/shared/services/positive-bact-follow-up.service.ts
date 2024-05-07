import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PositiveBactFollowUpDto, PositiveBactFollowUpSummaryDto } from '../models';
import { PositiveBactFollowUpDetailDto } from '../models/positive-bact-follow-up-details.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PositiveBactFollowUpService {
  positiveBactFollowUpEndpoint: string;
  positiveBactFollowUpSummaryEndpoint: string;
  bactCoComponentsSampleStatusEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.positiveBactFollowUpEndpoint = config.env.serverApiURL + '/v1/bacts';
    this.positiveBactFollowUpSummaryEndpoint = config.env.serverApiURL + '/v1/bact-summarys';
    this.bactCoComponentsSampleStatusEndpoint = config.env.serverApiURL + '/v1/bact-co-components';
  }

  public positiveBactFollowUpSearch(
    pageable,
    sortInfo?,
    filter?: {
      unitNumber?: string;
      statusDescriptionKeys?: string;
      locationIds?: string;
      from?: string;
      to?: string;
    }
  ): Observable<HttpResponse<PositiveBactFollowUpSummaryDto[]>> {
    const params = {
      ...pageable,
      ...sortInfo,
    };

    if (filter) {
      if (filter.unitNumber) {
        params.unitNumber = filter.unitNumber;
      }

      if (filter.statusDescriptionKeys) {
        params['status.in'] = filter.statusDescriptionKeys;
      }

      if (filter.locationIds) {
        params['testingLocationId.in'] = filter.locationIds;
      }

      if (filter.from) {
        params['drawDate.greaterThanOrEqual'] = filter.from;
      }

      if (filter.to) {
        params['drawDate.lessThanOrEqual'] = filter.to;
      }
    }

    return this.httpClient
      .get<PositiveBactFollowUpDto[]>(this.positiveBactFollowUpSummaryEndpoint, {
        params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getPositiveBactFollowUpById(id: number): Observable<HttpResponse<PositiveBactFollowUpDto>> {
    return this.httpClient
      .get<PositiveBactFollowUpDto>(this.positiveBactFollowUpEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public initFollowUp(obj: {
    id: number;
    gramStain?: string;
    organismId?: string;
  }): Observable<HttpResponse<PositiveBactFollowUpDto>> {
    return this.httpClient
      .put<PositiveBactFollowUpDto>(this.positiveBactFollowUpEndpoint + '/' + obj.id + '/init', obj, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getFollowUpDetails(id: number): Observable<HttpResponse<PositiveBactFollowUpDetailDto>> {
    return this.httpClient
      .get<PositiveBactFollowUpDetailDto>(this.positiveBactFollowUpEndpoint + '/' + id + '/follow-up-details', {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public updateBactSampleStatus(
    id: number,
    obj: { sampleStatus: string; sampleStatusComment: string },
    isCoComponents: boolean
  ): Observable<HttpResponse<PositiveBactFollowUpDto>> {
    return this.httpClient
      .put<PositiveBactFollowUpDto>(
        (isCoComponents ? this.bactCoComponentsSampleStatusEndpoint : this.positiveBactFollowUpEndpoint) +
          '/' +
          id +
          '/sample-status',
        obj,
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public editFinalInterpretation(
    id: number,
    obj: { appliedInterpretation: string; comment: string }
  ): Observable<HttpResponse<PositiveBactFollowUpDto>> {
    return this.httpClient
      .put<PositiveBactFollowUpDto>(this.positiveBactFollowUpEndpoint + '/' + id + '/applied-interpretation', obj, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public acceptFinalInterpretation(id: number): Observable<HttpResponse<PositiveBactFollowUpDto>> {
    return this.httpClient
      .put<PositiveBactFollowUpDto>(
        this.positiveBactFollowUpEndpoint + '/' + id + '/accept-system-interpretation',
        {},
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public closeFinalInterpretation(id: number, comment: string) {
    return this.httpClient
      .put<PositiveBactFollowUpDto>(
        this.positiveBactFollowUpEndpoint + '/' + id + '/close',
        { comment },
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
