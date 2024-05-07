import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProductFieldDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<ProductFieldDto[]>;

@Injectable({
  providedIn: 'root',
})
export class ProductFieldService {
  productDataEntryEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.productDataEntryEndpoint = config.env.serverApiURL + '/v1/product-fields';
  }

  public getProductFieldByCriteria(criteria?: object): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ProductFieldDto[]>(this.productDataEntryEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
