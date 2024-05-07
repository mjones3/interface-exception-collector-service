import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { StorageConfigurationDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type storageArrayConnectionResponse = HttpResponse<StorageConfigurationDto[]>;

@Injectable({
  providedIn: 'root',
})
export class StorageConfigurationService {
  readonly storageConfigurationEndpoint: string;

  constructor(private http: HttpClient, private envConfig: EnvironmentConfigService) {
    this.storageConfigurationEndpoint = envConfig.env.serverApiURL + '/v1/storage-configurations';
  }

  // storage-configuration
  getStorageConfigurationByParams(criteria?: { [key: string]: any }): Observable<storageArrayConnectionResponse> {
    return this.http
      .get<StorageConfigurationDto[]>(this.storageConfigurationEndpoint, {
        params: criteria,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
