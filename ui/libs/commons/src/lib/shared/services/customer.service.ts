import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { CustomerDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class CustomerService {
  customerEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.customerEndpoint = config.env.serverApiURL + '/v1/customers';
  }

  public getCustomerByCriteria(criteria?: {}): Observable<HttpResponse<CustomerDto[]>> {
    return this.httpClient
      .get<CustomerDto[]>(this.customerEndpoint, {
        params: { ...criteria, size: '20' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getCustomerById(id): Observable<HttpResponse<CustomerDto>> {
    return this.httpClient
      .get<CustomerDto>(`${this.customerEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
