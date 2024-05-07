import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ControlResultInterpretationRequestDTO, ControlResultInterpretationResponseDTO } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ControlResultService {
  controlResultEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.controlResultEndpoint = config.env.serverApiURL + '/v1/qc-control-results';
  }

  public interpret(
    payload: ControlResultInterpretationRequestDTO
  ): Observable<HttpResponse<ControlResultInterpretationResponseDTO>> {
    return this.httpClient
      .post<ControlResultInterpretationRequestDTO>(`${this.controlResultEndpoint}/interpret`, payload, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
