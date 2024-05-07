import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CodeStyleDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type codeTypeOptions = 'TEST_RESULT' | 'PQC_BACT' | 'EM_PRODUCT_STATUS';

@Injectable({
  providedIn: 'root',
})
export class CodeStyleService {
  codeStyleEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.codeStyleEndpoint = config.env.serverApiURL + '/v1/code-styles';
  }

  public getCodeStyles(codeType: codeTypeOptions): Observable<HttpResponse<CodeStyleDto[]>> {
    return this.httpClient
      .get<CodeStyleDto[]>(this.codeStyleEndpoint, {
        params: { codeType, page: '0', size: '100' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getCodeStyleByCode(codeType: string, code: string): Observable<HttpResponse<CodeStyleDto>> {
    return this.httpClient
      .get<CodeStyleDto>(this.codeStyleEndpoint, { params: { codeType, code }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getCodeStyleByCriteria(criteria?: { [key: string]: any }): Observable<CodeStyleDto[]> {
    return this.httpClient
      .get<CodeStyleDto[]>(this.codeStyleEndpoint, { params: { ...criteria } })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
