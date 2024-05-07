import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { ManufacturerDTO, Pageable } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ManufacturerService {
  private BASE_URI = '/v1/manufacturers';
  private BASE_URL = '';

  constructor(private httpClient: HttpClient, config: EnvironmentConfigService) {
    this.BASE_URL = config.env.serverApiURL + this.BASE_URI;
  }

  getManufacturerListByCriteria(criteria): Observable<ManufacturerDTO[]> {
    return this.httpClient
      .get<ManufacturerDTO[]>(this.BASE_URL, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(
        map(response => response.body),
        catchError(this.errorHandler)
      );
  }

  getManufacturerList(clientReqParams: any, pageable: Pageable): Observable<ManufacturerDTO[]> {
    return this.httpClient
      .get<ManufacturerDTO[]>(this.BASE_URL, {
        params: this.getReportParams(clientReqParams, pageable),
        observe: 'response',
      })
      .pipe(
        map(response => response.body),
        catchError(this.errorHandler)
      );
  }

  public getManufacturerById(id: number): Observable<HttpResponse<ManufacturerDTO>> {
    return this.httpClient
      .get<ManufacturerDTO>(`${this.BASE_URI}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  private getReportParams(clientReqParams: any, pageable: Pageable): any {
    const params = {
      ...clientReqParams,
      ...pageable,
    };
    return params;
  }

  private errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
