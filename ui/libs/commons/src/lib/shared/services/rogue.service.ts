import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RogueDto, RogueUnitReportDto, RogueUpdateStatusDto, TransactionResponseDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

//Common rest
@Injectable({
  providedIn: 'root',
})
export class RogueService {
  rogueEndpoint: string;
  transactionRogue: string;
  rogueUnitReportEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.rogueEndpoint = config.env.serverApiURL + '/v1/rogues';
    this.transactionRogue = config.env.serverApiURL + '/v1/rogues/transactions';
    this.rogueUnitReportEndpoint = this.rogueEndpoint + '/rogue-unit-report';
  }

  public getRogueById(id: number): Observable<HttpResponse<RogueDto>> {
    return this.httpClient
      .get<RogueDto>(`${this.rogueEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createRogue(dto: RogueDto): Observable<HttpResponse<RogueDto>> {
    return this.httpClient
      .post<RogueDto>(this.rogueEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateRogue(id: number, dto: RogueDto): Observable<HttpResponse<RogueDto>> {
    return this.httpClient
      .put<RogueDto>(`${this.rogueEndpoint}/${id}`, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteRogue(id: number): Observable<HttpResponse<RogueDto>> {
    return this.httpClient
      .delete<RogueDto>(`${this.rogueEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createRogueDonation(dto: any): Observable<HttpResponse<any>> {
    return this.httpClient
      .post<any>(this.transactionRogue, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public submitRogueStatusChange(rogueId: number, dto: RogueUpdateStatusDto): Observable<TransactionResponseDto> {
    return this.httpClient
      .put<TransactionResponseDto>(this.rogueEndpoint + '/' + rogueId + '/resolve', dto)
      .pipe(catchError(this.errorHandler));
  }

  public getRogueUnitReportDataByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<RogueUnitReportDto[]>> {
    return this.httpClient
      .get<RogueUnitReportDto[]>(this.rogueUnitReportEndpoint, {
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
