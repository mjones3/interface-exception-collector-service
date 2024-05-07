import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, delay } from 'rxjs/operators';
import { LockRecordDto } from '../models/lock.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class LockService {
  lockEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.lockEndpoint = config.env.serverApiURL + '/v1/locks';
  }

  public getLock(type: string, id: number | string, delayMilis?: number): Observable<HttpResponse<LockRecordDto>> {
    return this.httpClient
      .get<LockRecordDto>(`${this.lockEndpoint}/${type}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler), delay(delayMilis ?? 0));
  }

  public lock(type: string, id: number | string, delayMilis?: number): Observable<HttpResponse<LockRecordDto>> {
    return this.httpClient
      .post<LockRecordDto>(`${this.lockEndpoint}/${type}/${id}`, { id, type }, { observe: 'response' })
      .pipe(catchError(this.errorHandler), delay(delayMilis ?? 0));
  }

  public unlock(type: string, id: number | string, delayMilis?: number): Observable<LockRecordDto> {
    return this.httpClient
      .delete<LockRecordDto>(`${this.lockEndpoint}/${type}/${id}`)
      .pipe(catchError(this.errorHandler), delay(delayMilis ?? 0));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
