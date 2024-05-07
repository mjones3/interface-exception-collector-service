import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProcessModuleDto, ProcessPageDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type processPageResponse = HttpResponse<ProcessPageDto>;

//Common rest
@Injectable({
  providedIn: 'root'
})
export class ProcessPageService {
  processEndpoint: string;
  updateProcessEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.processEndpoint = config.env.serverApiURL + '/v1/processes/pages';
    this.updateProcessEndpoint = this.processEndpoint + '/{id}';
  }

  public getProcess(id: number): Observable<processPageResponse> {
    return this.httpClient.get<ProcessPageDto>(this.processEndpoint + '/' + id, { observe: 'response' }).pipe(catchError(this.errorHandler));
  }

  public getProcessByDescriptionBy(descriptionKey: string): Observable<HttpResponse<ProcessPageDto[]>> {
    return this.httpClient.get<ProcessPageDto[]>(`${this.processEndpoint}/?descriptionKey.in=${descriptionKey}`, { observe: 'response' });
  }

  public createProcess(dto: ProcessModuleDto): Observable<processPageResponse> {
    return this.httpClient.post<ProcessPageDto>(this.processEndpoint, dto, { observe: 'response' }).pipe(catchError(this.errorHandler));
  }

  public updateProcess(id: number, dto: ProcessModuleDto): Observable<processPageResponse> {
    return this.httpClient.put<ProcessPageDto>(this.replaceValueInUrl(this.updateProcessEndpoint, id), dto, { observe: 'response' }).pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  private replaceValueInUrl(url: string, value: any): string {
    return url.slice().replace('{id}', String(value));
  }

}

