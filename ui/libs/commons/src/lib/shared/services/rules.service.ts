import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RuleResponseDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class RulesService {
  rulesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.rulesEndpoint = config.env.serverApiURL + '/v1/rules';
  }

  public evaluation(inputs: any): Observable<HttpResponse<RuleResponseDto>> {
    return this.httpClient
      .post<RuleResponseDto>(`${this.rulesEndpoint}/evaluation`, inputs, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
