import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { WaferLotNumberDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class WaferLotNumberService {
  waferLotNumberEndpoint: string;
  lotsByPage: number;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.waferLotNumberEndpoint = config.env.serverApiURL + '/v1/lots';
    this.lotsByPage = 5;
  }

  public getAllWaferLotNumber(): Observable<HttpResponse<WaferLotNumberDto[]>> {
    return this.httpClient
      .get<WaferLotNumberDto>(this.waferLotNumberEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLotsByPage() {
    return this.lotsByPage;
  }

  public getWaferLotNumberByFacilityId(facilityId: number): Observable<HttpResponse<WaferLotNumberDto[]>> {
    return this.httpClient
      .get<WaferLotNumberDto>(this.waferLotNumberEndpoint + '?facilityId=' + facilityId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getWaferLotNumberByFacilityIdPagination(
    facilityId: number,
    page: number,
    size: number
  ): Observable<HttpResponse<WaferLotNumberDto[]>> {
    return this.httpClient
      .get<WaferLotNumberDto>(
        this.waferLotNumberEndpoint + '?facilityId=' + facilityId + '&page=' + page + '&size=' + size,
        { observe: 'response' }
      )
      .pipe(catchError(this.errorHandler));
  }

  public getWaferLotNumberByFacilityIdAndStatusPagination(
    facilityId: number,
    page: number,
    size: number,
    status: string
  ): Observable<HttpResponse<WaferLotNumberDto[]>> {
    return this.httpClient
      .get<WaferLotNumberDto>(
        this.waferLotNumberEndpoint +
          '?facilityId=' +
          facilityId +
          '&page=' +
          page +
          '&size=' +
          size +
          '&status=' +
          status,
        { observe: 'response' }
      )
      .pipe(catchError(this.errorHandler));
  }

  public getWaferLotNumberByFacilityIdAndStatusPaginationRangeOfDates(
    facilityId: number,
    page: number,
    size: number,
    status: string,
    from: string,
    to: string
  ): Observable<HttpResponse<WaferLotNumberDto[]>> {
    return this.httpClient
      .get<WaferLotNumberDto>(this.buildUrl(facilityId, page, size, status, from, to, 'createDate'), {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getWaferLotNumberByFacilityIdAndRangeOfDates(
    facilityId: number,
    from: string,
    to: string
  ): Observable<HttpResponse<WaferLotNumberDto[]>> {
    return this.httpClient
      .get<WaferLotNumberDto>(
        this.waferLotNumberEndpoint +
          '?facilityId=' +
          facilityId +
          '&createDate.lessThanOrEqual=' +
          to +
          '&createDate.greaterThanOrEqual=' +
          from,
        { observe: 'response' }
      )
      .pipe(catchError(this.errorHandler));
  }

  public addWaferLotNumber(newWaferLotNumberDto: WaferLotNumberDto): Observable<any> {
    return this.httpClient
      .post<any>(this.waferLotNumberEndpoint, newWaferLotNumberDto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public inactiveWaferLotNumber(id: number): Observable<any> {
    return this.httpClient
      .put<any>(this.waferLotNumberEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getWaferLotNumberByLotNumber(facilityId: number, lotNumber: string): Observable<any> {
    return this.httpClient
      .get<WaferLotNumberDto>(
        this.waferLotNumberEndpoint +
          '?facilityId=' +
          facilityId +
          '&lotNumber=' +
          escape(lotNumber) +
          '&status.in=ACTIVE,INACTIVE,EXPIRED' +
          '&includeDeleted=true',
        { observe: 'response' }
      )
      .pipe(catchError(this.errorHandler));
  }

  public getWaferLotNumberByLotNumberPagination(
    facilityId: number,
    lotNumber: string,
    page: number,
    size: number
  ): Observable<any> {
    return this.httpClient
      .get<WaferLotNumberDto>(
        this.waferLotNumberEndpoint +
          '?facilityId=' +
          facilityId +
          '&lotNumber=' +
          escape(lotNumber) +
          '&status.in=ACTIVE,INACTIVE,EXPIRED' +
          '&includeDeleted=true' +
          '&page=' +
          page +
          '&size=' +
          size,
        { observe: 'response' }
      )
      .pipe(catchError(this.errorHandler));
  }

  buildUrl(facilityId: number, page: number, size: number, status: string, from: string, to: string, sort: string) {
    let url: string = this.waferLotNumberEndpoint + '?facilityId=' + facilityId;
    if (page || page === 0) {
      url = url + '&page=' + page;
    }

    if (size) {
      url = url + '&size=' + size;
    }

    if (status) {
      url = url + '&status.in=' + status;
    }
    if (sort) {
      url = url + '&sort=' + sort;
    }

    if (from && to) {
      url = url + '&createDate.lessThanOrEqual=' + to + '&createDate.greaterThanOrEqual=' + from;
    }

    url = url + '&includeDeleted=true';

    return url;
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
