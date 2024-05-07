import { HttpClient, HttpErrorResponse, HttpHeaders, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { InventoryPoolDto, InventoryResponseDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class InventoryPoolService {
  readonly inventoryPoolUri = '/v1/inventories-pools';

  readonly httpOptions: Object = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
    observe: 'response',
  };

  constructor(private http: HttpClient, private config: EnvironmentConfigService) {}

  public createInventoryPool(inventoryPool: InventoryPoolDto): Observable<HttpResponse<InventoryPoolDto>> {
    return this.http
      .post<InventoryPoolDto>(
        `${this.config.env.serverApiURL}${this.inventoryPoolUri}`,
        inventoryPool,
        this.httpOptions
      )
      .pipe(catchError(this.errorHandler));
  }

  public updateInventoryPool(inventoryPool: InventoryPoolDto): Observable<HttpResponse<InventoryPoolDto>> {
    return this.http
      .put<InventoryPoolDto>(`${this.config.env.serverApiURL}${this.inventoryPoolUri}`, inventoryPool, this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  public searchInventoryPoolByFields(pooledInventoryId: number): Observable<HttpResponse<InventoryPoolDto[]>> {
    //add params to search by fields
    const params = new HttpParams().set('pooledInventoryId', String(pooledInventoryId));

    return this.http
      .get<InventoryResponseDto[]>(`${this.config.env.serverApiURL}${this.inventoryPoolUri}`, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllInventoriesPoolsByPooledInventoryId(
    pooledInventoryId: number
  ): Observable<HttpResponse<InventoryPoolDto[]>> {
    return this.http
      .get<InventoryPoolDto[]>(
        `${this.config.env.serverApiURL}${this.inventoryPoolUri}?pooledInventoryId=${pooledInventoryId}`,
        this.httpOptions
      )
      .pipe(catchError(this.errorHandler));
  }

  public getAllInventoriesPoolsByDonationIdChild(donationId: number): Observable<HttpResponse<InventoryResponseDto[]>> {
    return this.http
      .get<InventoryResponseDto[]>(
        `${this.config.env.serverApiURL}${this.inventoryPoolUri}/${donationId}/inventories-by-child-donation`,
        this.httpOptions
      )
      .pipe(catchError(this.errorHandler));
  }

  public getAllInventoriesPoolsByInventoryIdChild(
    inventoryId: number
  ): Observable<HttpResponse<InventoryResponseDto[]>> {
    return this.http
      .get<InventoryResponseDto[]>(
        `${this.config.env.serverApiURL}${this.inventoryPoolUri}/${inventoryId}/inventories-by-child-inventory`,
        this.httpOptions
      )
      .pipe(catchError(this.errorHandler));
  }

  public removeInventoryFromPools(
    inventoryId: number,
    poolInventoryId: number
  ): Observable<HttpResponse<InventoryResponseDto[]>> {
    return this.http
      .delete<InventoryResponseDto[]>(
        `${this.config.env.serverApiURL}${this.inventoryPoolUri}/${poolInventoryId}/${inventoryId}`,
        this.httpOptions
      )
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
