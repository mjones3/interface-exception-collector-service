import { HttpClient, HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BarcodeParts } from '../models';
import { BarcodeTranslationResponseDTO } from '../models/barcode-translation-response.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class BarcodeService {
  private barcodeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.barcodeEndpoint = config.env.serverApiURL + '/v1/barcodes';
  }

  getBarcodeParts(barcode: string): Observable<BarcodeParts> {
    return this.httpClient.get<BarcodeParts>(`${this.barcodeEndpoint}/${barcode}`);
  }

  getBarcodeTranslation(barcode: string): Observable<HttpResponse<BarcodeTranslationResponseDTO>> {
    const params = new HttpParams().set('barcode', barcode);
    return this.httpClient
      .get<BarcodeTranslationResponseDTO>(`${this.barcodeEndpoint}/translations/translate`, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
