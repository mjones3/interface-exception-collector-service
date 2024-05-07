import { HttpClient, HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PqaDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

interface PQARequest {
  inventoryId: string;
  motivationKey: string;
  type: string;
  facilityId: number;
}

interface PQARareDonorValidationRequest {
  inventoryId: number;
  facilityId: number;
}

@Injectable({
  providedIn: 'root',
})
export class PqaService {
  pqaValidationsEndpoint: string;
  pqaRareDonorValidation: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.pqaValidationsEndpoint = config.env.serverApiURL + '/v1/pqa/validations';
    this.pqaRareDonorValidation = this.pqaValidationsEndpoint + '/rareDonor';
  }

  public validate(request: PQARequest): Observable<PqaDto[]> {
    return this.httpClient.post<PqaDto[]>(this.pqaValidationsEndpoint, request);
  }

  public validateRareDonor(request: PQARareDonorValidationRequest): Observable<PqaDto[]> {
    return this.httpClient.post(this.pqaRareDonorValidation, request).pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
