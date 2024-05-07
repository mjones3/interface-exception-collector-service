import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AntigenReactionStrengthDto } from '../models/antigen-reaction-strength-configurations.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class AntigenReactionStrengthConfigurationsService {
  antigenReactionStrengthConfigurationsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.antigenReactionStrengthConfigurationsEndpoint =
      config.env.serverApiURL + '/v1/antigen-reaction-strength-configurations';
  }

  public getAntigenReactionStrengthByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<AntigenReactionStrengthDto[]>> {
    return this.httpClient
      .get<AntigenReactionStrengthDto[]>(this.antigenReactionStrengthConfigurationsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
