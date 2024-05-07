import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProcessModuleDto, ProcessProductDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<ProcessProductDto>;
type EntityResponseTypeList = HttpResponse<ProcessProductDto[]>;

//Common rest
@Injectable({ providedIn: 'root' })
export class ProcessProductService {
  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {}

  getBaseUrl() {
    return this.config.env.serverApiURL + '/v1/processes/products';
  }

  public getProcessProduct(id: string): Observable<EntityResponseType> {
    return this.httpClient
      .get<ProcessProductDto>(`${this.getBaseUrl()}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getProcessProductList(): Observable<EntityResponseTypeList> {
    return this.httpClient
      .get<ProcessProductDto>(`${this.getBaseUrl()}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createProcessProduct(dto: ProcessModuleDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<ProcessProductDto>(this.getBaseUrl(), dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateProcessProduct(id: string, dto: ProcessModuleDto): Observable<EntityResponseType> {
    return this.httpClient
      .put<ProcessProductDto>(`${this.getBaseUrl()}/${id}`, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
