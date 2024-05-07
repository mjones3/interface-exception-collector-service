import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DrawDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<DrawDto>;
type EntityArrayResponseType = HttpResponse<DrawDto[]>;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class DrawService {
  drawEndpoint: string;
  updateDrawEndpoint: string;
  deleteDrawEndpoint: string;
  findDrawByDonationId: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.drawEndpoint = config.env.serverApiURL + '/v1/draws';
    this.updateDrawEndpoint = this.drawEndpoint + '/{donationId}';
    this.deleteDrawEndpoint = this.drawEndpoint + '/{donationId}';
    this.findDrawByDonationId = this.drawEndpoint + '/{donationId}';
  }

  public getDrawByDonationId(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DrawDto>(this.replaceValueInUrl(this.findDrawByDonationId, id), { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDrawByCriteria(criteria, pageable?, sortInfo?): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DrawDto[]>(this.drawEndpoint, {
        params: { ...criteria, ...pageable, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createDraw(dto: DrawDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<DrawDto>(this.drawEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateDraw(id: number, dto: DrawDto): Observable<EntityResponseType> {
    return this.httpClient
      .put<DrawDto>(this.replaceValueInUrl(this.updateDrawEndpoint, id), dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDraw(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .delete<DrawDto>(this.replaceValueInUrl(this.updateDrawEndpoint, id), { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  private replaceValueInUrl(url: string, value: any): string {
    return url.slice().replace('{donationId}', String(value));
  }
}
