import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CentrifugeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class CentrifugeService {
  readonly centrifugeUri = 'v1/centrifuges';

  readonly httpOptions: Object = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
    observe: 'response',
  };

  constructor(private http: HttpClient, private config: EnvironmentConfigService) {}

  // Initialize Centrifuge DTO
  initCentrifuge(): CentrifugeDto {
    return {
      centrifugeTypeId: null,
      processDeviceId: null,
    };
  }

  public addCentrifuge(centrifugeDto: CentrifugeDto): Observable<HttpResponse<CentrifugeDto>> {
    return this.http
      .post<CentrifugeDto>(`${this.config.env.serverApiURL}/${this.centrifugeUri}`, centrifugeDto, this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  public saveCentrifugeAndUpdateInventory(centrifugeDto: CentrifugeDto): Observable<HttpResponse<any>> {
    return this.http
      .post<CentrifugeDto>(`${this.config.env.serverApiURL}/${this.centrifugeUri}`, centrifugeDto, this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
