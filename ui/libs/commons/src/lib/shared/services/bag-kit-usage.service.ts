import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BagKitUsageDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<BagKitUsageDto[]>;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class BagKitUsageService {
  bagTypeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.bagTypeEndpoint = config.env.serverApiURL + '/v1/bag-kit-usages';
  }

  public getBagKitUsageByDonationId(donationId: string): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<BagKitUsageDto[]>(this.bagTypeEndpoint, { params: { donationId }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
