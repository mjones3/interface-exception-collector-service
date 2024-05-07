import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DriveDto } from '../models/drive.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DriveService {
  drivesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.drivesEndpoint = config.env.serverApiURL + '/v1/drives';
  }

  public getDrivesByCriteria(criteria?: {}): Observable<HttpResponse<DriveDto[]>> {
    return this.httpClient
      .get<DriveDto[]>(this.drivesEndpoint, {
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
