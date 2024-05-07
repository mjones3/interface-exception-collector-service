import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReleaseItemDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<ReleaseItemDto>;
type EntityArrayResponseType = HttpResponse<ReleaseItemDto[]>;

@Injectable({
  providedIn: 'root',
})
export class ReleaseItemService {
  releaseItemEndpoint: string;
  releaseItemsByTypeEndpoint: string;
  releaseItemElegiblityRulesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.releaseItemEndpoint = config.env.serverApiURL + '/v1/release-items';
    this.releaseItemsByTypeEndpoint = `${this.releaseItemEndpoint}-types`;
    this.releaseItemElegiblityRulesEndpoint = `${this.releaseItemEndpoint}-eligibility-rules`;
  }

  public getItemsByReleaseTypeId(typeId: string): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReleaseItemDto[]>(this.releaseItemsByTypeEndpoint + '/' + typeId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getItemsByReleasElegibilityRules(criteria?: { [key: string]: string }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReleaseItemDto[]>(this.releaseItemElegiblityRulesEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getItemsByReleaseTypeIdAndInventories(
    typeId: string,
    inventoryIds: string
  ): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReleaseItemDto[]>(this.releaseItemsByTypeEndpoint, {
        params: { typeId, inventoryIds },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
