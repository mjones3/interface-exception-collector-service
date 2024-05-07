import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { AntigenMappingConfigurationDto } from '../models/antigen-mapping-configuration.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class AntigenMappingConfigurationsService {
  antigenMappingConfigurationsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.antigenMappingConfigurationsEndpoint = config.env.serverApiURL + '/v1/antigen-mapping-configurations';
  }

  public getAntigenMappingConfigurationsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<AntigenMappingConfigurationDto[]>> {
    return this.httpClient
      .get<AntigenMappingConfigurationDto[]>(this.antigenMappingConfigurationsEndpoint, {
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
