import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TreoMockApiService } from '@treo';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProcessModuleDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type processModuleResponse = HttpResponse<ProcessModuleDto>;

//Common rest
@Injectable({
  providedIn: 'root'
})
export class ProcessModuleService {
  processEndpoint: string;
  updateProcessEndpoint: string;

  constructor(private mockAPi: TreoMockApiService, private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.processEndpoint = config.env.serverApiURL + '/v1/processes/modules';
    this.updateProcessEndpoint = this.processEndpoint + '/{id}';
  }

  public getProcess(id: number): Observable<processModuleResponse> {
    return this.httpClient.get<ProcessModuleDto>(this.processEndpoint + '/' + id, { observe: 'response' }).pipe(catchError(this.errorHandler));
  }

  public createProcess(dto: ProcessModuleDto): Observable<processModuleResponse> {
    return this.httpClient.post<ProcessModuleDto>(this.processEndpoint, dto, { observe: 'response' }).pipe(catchError(this.errorHandler));
  }

  public updateProcess(id: number, dto: ProcessModuleDto): Observable<processModuleResponse> {
    return this.httpClient.put<ProcessModuleDto>(this.replaceValueInUrl(this.updateProcessEndpoint, id), dto, { observe: 'response' }).pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  private replaceValueInUrl(url: string, value: any): string {
    return url.slice().replace('{id}', String(value));
  }

}

