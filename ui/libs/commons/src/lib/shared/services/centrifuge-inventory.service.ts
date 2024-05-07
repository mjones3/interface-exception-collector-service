import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {CentrifugeInventoryDto} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root'
})
export class CentrifugeInventoryService {

  readonly inventoryCentrifuge = 'v1/inventory-centrifuges';

  readonly httpOptions: Object = {
    headers:  new HttpHeaders({'Content-Type': 'application/json'}),
    observe: 'response'
  };

  constructor(private http: HttpClient, private config: EnvironmentConfigService) {}

  // Initialize Centrifuge DTO
  initCentrifugeInventory(centrifugeId?:number,inventoryId?:number,centrifugeTypeId?:number): CentrifugeInventoryDto {
    return {
        centrifugeId: centrifugeId,
        inventoryId: inventoryId,
        centrifugeTypeId: centrifugeTypeId,
    };
  }

  public addInventoryCentrifuge(inventoryCentrifuge: CentrifugeInventoryDto): Observable<HttpResponse<CentrifugeInventoryDto>>{
    return this.http.post<CentrifugeInventoryDto>(`${this.config.env.serverApiURL}/${this.inventoryCentrifuge}`,
      inventoryCentrifuge, this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  public updateInventoryCentrifuge(inventoryCentrifuge: CentrifugeInventoryDto): Observable<HttpResponse<CentrifugeInventoryDto>>{
    return this.http.put<CentrifugeInventoryDto>(`${this.config.env.serverApiURL}/${this.inventoryCentrifuge}`,
      inventoryCentrifuge, this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  public deleteInventoryCentrifuge(inventoryCentrifugeId: number): Observable<HttpResponse<any>>{
    return this.http.delete<any>(`${this.config.env.serverApiURL}/${this.inventoryCentrifuge}/${inventoryCentrifugeId}`,
      this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }


}
