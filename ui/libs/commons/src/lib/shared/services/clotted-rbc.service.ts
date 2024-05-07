import { HttpClient, HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BarcodeParts, RuleResponseDto } from '../models';
import { BarcodeTranslationResponseDTO } from '../models/barcode-translation-response.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ClottedRbcService {
  statusUri: string;
  flaggingUri: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.flaggingUri = config.env.serverApiURL + '/v1/clotted-rbcs/flags';
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  public callFlagging(inputs: any): Observable<any> {
    return this.httpClient
      .post<RuleResponseDto>(this.flaggingUri, inputs, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }
}
