import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DocumentDto, FieldDTO } from '../models';
import { EnvironmentConfigService } from './environment-config.service';
type EntityArrayResponseType = HttpResponse<FieldDTO[]>;

@Injectable({
  providedIn: 'root',
})
export class FieldService {
  qcFieldEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.qcFieldEndpoint = config.env.serverApiURL + '/v1/fields';
  }

  public getFieldsByCriteria(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DocumentDto[]>(this.qcFieldEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
