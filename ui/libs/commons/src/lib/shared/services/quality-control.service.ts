import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {QualityControlDto} from '../models';
import {EnvironmentConfigService} from './environment-config.service';

type qualityControlResponse = HttpResponse<QualityControlDto[]>;

//Common rest
@Injectable({
  providedIn: 'root'
})
export class QualityControlService {
  qualityControlsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.qualityControlsEndpoint = config.env.serverApiURL + '/v1/quality-controls';
  }

  public getQualityControls(): Observable<qualityControlResponse> {
    return this.httpClient.get<QualityControlDto[]>(this.qualityControlsEndpoint, {observe: 'response'}).pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}

