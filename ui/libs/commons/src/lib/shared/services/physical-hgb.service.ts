import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PhysicalHGBDto } from '../models/physical-hgb.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PhysicalHGBService {
  physicalHGBEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.physicalHGBEndpoint = config.env.serverApiURL + '/v1/physical-hgbs';
  }

  public getPhysicalHGB(id: number): Observable<HttpResponse<PhysicalHGBDto>> {
    return this.httpClient
      .get<PhysicalHGBDto>(this.physicalHGBEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getPhysicalHGBByPhysicalId(physicalId: number): Observable<HttpResponse<PhysicalHGBDto[]>> {
    return this.httpClient
      .get<PhysicalHGBDto[]>(this.physicalHGBEndpoint + '?physicalId=' + physicalId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
