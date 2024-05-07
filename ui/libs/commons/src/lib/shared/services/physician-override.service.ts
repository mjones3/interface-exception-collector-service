import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PhysicianOverrideDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

const TYPE_KEY = 'physician.override.label';

@Injectable({
  providedIn: 'root',
})
export class PhysicianOverrideService {
  physicianOverrideEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.physicianOverrideEndpoint = config.env.serverApiURL + '/v1/eligibility-overrides?typeKey=' + TYPE_KEY;
  }

  public getPhysicianOverridyByDonationId(donationId: string): Observable<HttpResponse<PhysicianOverrideDto[]>> {
    return this.httpClient
      .get<PhysicianOverrideDto[]>(this.physicianOverrideEndpoint, { params: { donationId }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
