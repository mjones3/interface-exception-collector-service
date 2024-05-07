import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { BagTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<BagTypeDto[]>;
type EntityResponseType = HttpResponse<BagTypeDto>;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class BagTypeService {
  bagTypeEndpoint: string;
  updateBagTypeEndpoint: string;
  deleteBagTypeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.bagTypeEndpoint = config.env.serverApiURL + '/v1/bag-types';
  }

  public getBagTypes(): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<BagTypeDto>(this.bagTypeEndpoint, { params: { size: '1000' }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getBagType(id: string): Observable<EntityResponseType> {
    return this.httpClient
      .get<BagTypeDto>(this.bagTypeEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getBagTypeByCriteria(criteria, pageable, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<BagTypeDto[]>(this.bagTypeEndpoint, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getBagTypesByMotivationAndDonation(motivationId: string, donationTypeId: string): Observable<any> {
    const url = this.getUrlBagTypesByMotivationAndDonations(motivationId, donationTypeId);
    return this.httpClient
      .get<BagTypeDto>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createBagType(dto: BagTypeDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<BagTypeDto>(this.bagTypeEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateBagType(id: number, dto: BagTypeDto): Observable<EntityResponseType> {
    return this.httpClient
      .put<BagTypeDto>(this.bagTypeEndpoint + '/' + id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteBagType(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .delete<BagTypeDto>(this.bagTypeEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  getUrlBagTypesByMotivationAndDonations(motivationId: string, donationTypeId: string) {
    return `${this.config.env.serverApiURL}/v1/motivations/${motivationId}/donation-types/${donationTypeId}/bag-types`;
  }
}
