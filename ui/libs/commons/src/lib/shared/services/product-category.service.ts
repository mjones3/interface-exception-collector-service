import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProductCategoryDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<ProductCategoryDto>;
type EntityArrayResponseType = HttpResponse<ProductCategoryDto[]>;

@Injectable({
  providedIn: 'root',
})
export class ProductCategoryService {
  productCategoriesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.productCategoriesEndpoint = config.env.serverApiURL + '/v1/product-categories';
  }

  public getProductCategoryById(id: number): Observable<ProductCategoryDto> {
    return this.httpClient
      .get<ProductCategoryDto>(this.productCategoriesEndpoint + '/' + id)
      .pipe(catchError(this.errorHandler));
  }

  public getAllProductCategories(params?: object): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ProductCategoryDto[]>(this.productCategoriesEndpoint + '/rule-engine', {
        params: { ...params },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllProductCategoriesByType(type: string, params?: {}): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ProductCategoryDto[]>(this.productCategoriesEndpoint, {
        params: { type, ...params },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getProductCategoriesByCriteria(criteria?: {}): Observable<ProductCategoryDto[]> {
    return this.httpClient
      .get<ProductCategoryDto[]>(this.productCategoriesEndpoint, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
