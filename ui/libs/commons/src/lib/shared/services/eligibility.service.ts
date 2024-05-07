import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { VolumeCalculationRequestDto, VolumeCalculationResponseDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class EligibilityService {
  volumeCalculationEndpoint: string;
  readWeightEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.volumeCalculationEndpoint = config.env.serverApiURL + '/v1/volume-calculations/calculate';
    this.readWeightEndpoint = 'http://localhost:8282/agent/scale';
  }

  public getVolume(
    calculationRequest: VolumeCalculationRequestDto
  ): Observable<HttpResponse<VolumeCalculationResponseDto>> {
    return this.httpClient
      .post<VolumeCalculationResponseDto>(this.volumeCalculationEndpoint, calculationRequest, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public readWeight(): Observable<any> {
    return this.httpClient
      .get<any>(this.readWeightEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
