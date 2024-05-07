import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PhysicalDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PhysicalService {
  physicalEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.physicalEndpoint = config.env.serverApiURL + '/v1/physicals';
  }

  public getPhysicalByDonationId(donationId: number): Observable<HttpResponse<PhysicalDto[]>> {
    return this.httpClient
      .get<PhysicalDto[]>(this.physicalEndpoint + '?donationId=' + donationId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
