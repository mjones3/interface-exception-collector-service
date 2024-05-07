import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PhysicalExamDto, PhysicalExamResultDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PhysicalExamService {
  physicalExamEndpoint: string;
  physicalExamResultEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.physicalExamEndpoint = `${config.env.serverApiURL}/v1/physical-exams`;
    this.physicalExamResultEndpoint = `${config.env.serverApiURL}/v1/physical-exam-results`;
  }

  public getPhysicalExamsByCriteria(criteria:{[key: string]: any}): Observable<HttpResponse<PhysicalExamDto[]>> {
    return this.httpClient
      .get<PhysicalExamDto[]>(this.physicalExamEndpoint, { observe: 'response', params: criteria })
      .pipe(catchError(this.errorHandler));
  }

  public getPhysicalExamResultsByCriteria(criteria:{[key: string]: any}): Observable<HttpResponse<PhysicalExamResultDto[]>> {
    return this.httpClient
      .get<PhysicalExamDto[]>(this.physicalExamResultEndpoint, { observe: 'response', params: criteria })
      .pipe(catchError(this.errorHandler));
  }
  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
