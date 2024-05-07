import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  ArbovirusCaseHistoryDto,
  ArbovirusDto,
  ArbovirusOpenCaseDto,
  ArbovirusPotentialNoCaseDto,
  ArbovirusTypeDto,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ArbovirusService {
  arbovirusEndpoint: string;
  arbovirusTypeEndpoint: string;
  arbovirusCaseHistory: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.arbovirusEndpoint = config.env.serverApiURL + '/v1/arbovirus';
    this.arbovirusTypeEndpoint = config.env.serverApiURL + '/v1/arbovirus-types';
    this.arbovirusCaseHistory = config.env.serverApiURL + '/v1/arbovirus-case-histories';
  }

  public caseOpened = new BehaviorSubject<boolean>(true);
  public potentialCaseEdited = new BehaviorSubject<boolean>(false);
  public noCaseSaved = new BehaviorSubject<boolean>(false);

  //ARBOVIRUS

  public getArbovirusByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<ArbovirusDto[]>> {
    return this.httpClient
      .get<ArbovirusDto[]>(this.arbovirusEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getArbovirusTriggerZipCodes(): Observable<string[]> {
    return this.httpClient
      .get<string[]>(`${this.arbovirusEndpoint}/trigger-zip-codes`)
      .pipe(catchError(this.errorHandler));
  }

  public createArbovirus(arbovirus: ArbovirusDto): Observable<ArbovirusDto> {
    return this.httpClient
      .post<ArbovirusDto>(`${this.arbovirusEndpoint}`, arbovirus)
      .pipe(catchError(this.errorHandler));
  }

  public updateArbovirus(id: number, arbovirus: Partial<ArbovirusDto>): Observable<ArbovirusDto> {
    return this.httpClient
      .put<ArbovirusDto>(`${this.arbovirusEndpoint}/${id}`, arbovirus)
      .pipe(catchError(this.errorHandler));
  }

  public openCase(id: string, arbovirus: ArbovirusOpenCaseDto): Observable<ArbovirusDto> {
    return this.httpClient
      .put<ArbovirusDto>(`${this.arbovirusEndpoint}/${id}/open-case`, arbovirus)
      .pipe(catchError(this.errorHandler));
  }

  public closeCase(id: number, comments: string): Observable<ArbovirusDto> {
    return this.httpClient
      .put<ArbovirusDto>(`${this.arbovirusEndpoint}/${id}/close-case`, { comments })
      .pipe(catchError(this.errorHandler));
  }

  public createArbovirusCaseHistory(
    arbovirusCaseHistoryDto: ArbovirusCaseHistoryDto
  ): Observable<ArbovirusCaseHistoryDto> {
    return this.httpClient
      .post<ArbovirusCaseHistoryDto>(`${this.arbovirusCaseHistory}`, arbovirusCaseHistoryDto)
      .pipe(catchError(this.errorHandler));
  }

  //ARBOVIRUS TYPES

  public getArbovirusTypesByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<ArbovirusTypeDto[]>> {
    return this.httpClient
      .get<ArbovirusTypeDto[]>(this.arbovirusTypeEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getArbovirusCaseHistoryByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<ArbovirusCaseHistoryDto[]>> {
    return this.httpClient
      .get<ArbovirusCaseHistoryDto[]>(this.arbovirusCaseHistory, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getArbovirusById(id: string | number): Observable<HttpResponse<ArbovirusDto>> {
    return this.httpClient
      .get<ArbovirusDto>(`${this.arbovirusEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public closeNoCase(id: number, dto: ArbovirusPotentialNoCaseDto): Observable<ArbovirusDto> {
    return this.httpClient
      .put<ArbovirusDto>(`${this.arbovirusEndpoint}/${id}/potential-no-case`, dto)
      .pipe(catchError(this.errorHandler));
  }

  defaultNoCase() {
    this.noCaseSaved.next(false);
  }

  editPotentialCase() {
    this.potentialCaseEdited.next(true);
  }
  defaultPotentialCase() {
    this.potentialCaseEdited.next(false);
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
