import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { EligibilityOverrideDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

const TYPE_KEY = [
  'eligibility-override.eligibility.label',
  'eligibility-override.health-history.label',
  'eligibility-override.age.label',
  'eligibility-override.physical.label',
];

@Injectable({
  providedIn: 'root',
})
export class EligibilityOverrideService {
  eligibilityOverrideEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.eligibilityOverrideEndpoint = config.env.serverApiURL + '/v1/eligibility-overrides?typeKey.in=' + TYPE_KEY;
  }

  public getEligibilityOverridyByDonationId(donationId: string): Observable<HttpResponse<EligibilityOverrideDto[]>> {
    return this.httpClient
      .get<EligibilityOverrideDto[]>(this.eligibilityOverrideEndpoint, { params: { donationId }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
