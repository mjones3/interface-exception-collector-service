import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PhysicalBPDto } from '../models/physical-bp.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PhysicalBPService {
  physicalBPEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.physicalBPEndpoint = config.env.serverApiURL + '/v1/physical-bps';
  }

  public getPhysicalBP(id: number): Observable<HttpResponse<PhysicalBPDto>> {
    return this.httpClient
      .get<PhysicalBPDto>(this.physicalBPEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getPhysicalBPByPhysicalId(physicalId: number): Observable<HttpResponse<PhysicalBPDto[]>> {
    return this.httpClient
      .get<PhysicalBPDto[]>(this.physicalBPEndpoint + '?physicalId=' + physicalId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
