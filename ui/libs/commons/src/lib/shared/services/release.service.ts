import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReleaseDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<ReleaseDto>;
type EntityArrayResponseType = HttpResponse<ReleaseDto[]>;

@Injectable({
  providedIn: 'root',
})
export class ReleaseService {
  releaseEndpoint: string;
  releaseExportEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.releaseEndpoint = config.env.serverApiURL + '/v1/releases';
    this.releaseExportEndpoint = config.env.serverApiURL + '/v1/prints/{fileType}/EMERGENCY_EXCEPTIONAL_RELEASE';
  }

  public createRelease(dto: ReleaseDto): Observable<EntityResponseType> {
    return this.httpClient.post<ReleaseDto>(this.releaseEndpoint, dto, { observe: 'response' });
  }

  public getRelease(donationId, pageable, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReleaseDto[]>(this.releaseEndpoint, {
        params: { donationId, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
