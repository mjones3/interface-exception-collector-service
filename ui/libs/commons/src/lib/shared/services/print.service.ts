import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PrintDto, PrintExtesion, PrintType } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

//Common rest
@Injectable({
  providedIn: 'root',
})
export class PrintService {
  printEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.printEndpoint = config.env.serverApiURL + '/v1/prints';
  }

  public print(extension: PrintExtesion, type: PrintType, optionalParams?: {}): Observable<HttpResponse<PrintDto>> {
    return this.httpClient
      .get<any>(this.printEndpoint, {
        params: { extension, type, ...optionalParams },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
