import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PhysicalPulseDto } from '../models/physical-pulse.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PhysicalPulseService {
  physicalPulseEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.physicalPulseEndpoint = config.env.serverApiURL + '/v1/physical-pulses';
  }

  public getPhysicalPulse(id: number): Observable<HttpResponse<PhysicalPulseDto>> {
    return this.httpClient
      .get<PhysicalPulseDto>(this.physicalPulseEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getPhysicalPulseByPhysicalId(physicalId: number): Observable<HttpResponse<PhysicalPulseDto[]>> {
    return this.httpClient
      .get<PhysicalPulseDto[]>(this.physicalPulseEndpoint + '?physicalId=' + physicalId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
