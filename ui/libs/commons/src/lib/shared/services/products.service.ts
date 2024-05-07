import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { InventoryDto, ProductDto } from '../models';
import { ProductConversionDto } from '../models/product-conversion.dto';
import { ProductInformationDto } from '../models/product-information.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<ProductDto[]>;

declare var dT_: any;

@Injectable({
  providedIn: 'root',
})
export class ProductsService {
  readonly productsUrl: string;
  readonly finalStatus: string;
  availableProductsConversionsEndpoint: string;
  productsConversionsEndpoint: string;
  productCodeValidationUrl: string;
  productsInformationEndpoint: string;

  constructor(private httpClient: HttpClient, private envConfig: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.productsUrl = envConfig.env.serverApiURL + '/v1/products';
    this.productCodeValidationUrl = envConfig.env.serverApiURL + '/v1/labels/validate';
    this.availableProductsConversionsEndpoint = envConfig.env.serverApiURL + '/v1/product-conversion/available-product';
    this.productsConversionsEndpoint = envConfig.env.serverApiURL + '/v1/product-conversions';
    this.productsInformationEndpoint = envConfig.env.serverApiURL + '/v1/products-information';
    this.finalStatus = 'FINAL';
  }

  getProduct(productCode: string): Observable<any> {
    return this.httpClient.get(`${this.productsUrl}/${productCode}`);
  }

  getAllProductCategories(): Observable<any> {
    return this.httpClient.get(`${this.envConfig.env.serverApiURL}/v1/product-categories`, { observe: 'response' });
  }

  getProductCategoryByLabel(): Observable<any> {
    return this.httpClient.get(`${this.envConfig.env.serverApiURL}/v1/product-categories?type=LABEL_SELECTION`, {
      observe: 'response',
    });
  }

  getProductCategoriesByType(type: string): Observable<any> {
    return this.httpClient.get(`${this.envConfig.env.serverApiURL}/v1/product-categories?type=${type}`, {
      observe: 'response',
    });
  }

  getProductsDescription(inventory: InventoryDto[]): Observable<EntityArrayResponseType> {
    const productsIds = [];
    inventory.forEach(inv => {
      if (productsIds.indexOf(inv.productCode) === -1) {
        productsIds.push(inv.productCode);
      }
    });
    return this.httpClient.get<ProductDto[]>(`${this.productsUrl}?productCode.in=${productsIds}`, {
      observe: 'response',
    });
  }

  getProductsByProductCodes(productsIds: string[]): Observable<EntityArrayResponseType> {
    return this.httpClient.get<ProductDto[]>(`${this.productsUrl}?productCode.in=${productsIds}`, {
      observe: 'response',
    });
  }

  getFinalProductsByProductCodes(productsCodes: string[]): Observable<EntityArrayResponseType> {
    return this.httpClient.get<ProductDto[]>(`${this.productsUrl}?productCode.in=${productsCodes}&status=${this.finalStatus}`, {
      observe: 'response',
    });
  }

  getAvailableProductConversions(type: string, inventoryId: string): Observable<HttpResponse<ProductConversionDto[]>> {
    return this.httpClient.get<ProductConversionDto[]>(
      `${this.availableProductsConversionsEndpoint}/${type}/${inventoryId}`,
      {
        observe: 'response',
      }
    );
  }

  getProductConversions(type: string, fromProductCode: string): Observable<HttpResponse<ProductConversionDto[]>> {
    return this.httpClient.get<ProductConversionDto[]>(
      `${this.productsConversionsEndpoint}?conversionType.in=${type}&fromProduct=${fromProductCode}`,
      {
        observe: 'response',
      }
    );
  }

  getProductConversionsToProduct(
    type: string,
    toProductCode: string
  ): Observable<HttpResponse<ProductConversionDto[]>> {
    return this.httpClient.get<ProductConversionDto[]>(
      `${this.productsConversionsEndpoint}?conversionType.in=${type}&toProduct=${toProductCode}`,
      {
        observe: 'response',
      }
    );
  }

  getProductConversionsByType(type: string): Observable<HttpResponse<ProductConversionDto[]>> {
    return this.httpClient.get<ProductConversionDto[]>(
      `${this.productsConversionsEndpoint}?conversionType.in=${type}`,
      {
        observe: 'response',
      }
    );
  }

  public prodcutCodeValidation(productCode: string, unitNumber: string): Observable<any> {
    const dto = {
      unitNumber: unitNumber,
      productCode: productCode,
    };
    return this.httpClient
      .post<any>(this.productCodeValidationUrl, dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  public findProductsInformation(filters: any) {
    return this.httpClient
      .get<ProductInformationDto>(this.productsInformationEndpoint, {
        params: { ...filters },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }
}
