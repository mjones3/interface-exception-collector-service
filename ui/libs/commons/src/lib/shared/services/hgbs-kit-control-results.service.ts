import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  FieldDTO,
  HgbsControlResultInterpretationRequestDTO,
  HgbsControlResultInterpretationResponseDTO,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class HgbsKitControlsResultsService {
  hgbsKitControlsResultsFieldsEndpoint: string;
  hgbsKitControlsResultsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.hgbsKitControlsResultsFieldsEndpoint = config.env.serverApiURL + '/v1/hgbs-kit-control-result-fields';
    this.hgbsKitControlsResultsEndpoint = config.env.serverApiURL + '/v1/hgbs-kit-control-results';
  }

  public getHgbsKitControlResultsFieldsByCriteria(criteria: {}): Observable<HttpResponse<FieldDTO>> {
    return this.httpClient
      .get<FieldDTO[]>(this.hgbsKitControlsResultsFieldsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public interpret(
    payload: HgbsControlResultInterpretationRequestDTO
  ): Observable<HttpResponse<HgbsControlResultInterpretationResponseDTO>> {
    return this.httpClient
      .post<HgbsControlResultInterpretationRequestDTO>(`${this.hgbsKitControlsResultsEndpoint}/interpret`, payload, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
