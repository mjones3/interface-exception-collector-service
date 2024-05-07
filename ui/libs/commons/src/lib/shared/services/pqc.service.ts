import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TransactionResponseDto } from '../models';
import {
  DoubleBlindResponseDto,
  PqcBatchDto,
  PqcMeanTestResultDto,
  PqcSampleTransactionDto,
  PqcTestCalculationRequest,
  PqcTestCalculationResponse,
  PqcTestFieldDto,
  PqcTestResultDetailsDto,
  PqcTestResultDto,
  PqcTestTypeDto,
} from '../models/pqc.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PqcService {
  private pqcEndpoint: string;
  private pqcTestType: string;
  private pqcTestField: string;
  private pqcBatch: string;
  private pqcTestResult: string;
  private pqcMeanTestResult: string;
  private pqcDoubleBlindReview: string;
  private sampleEndpoint: string;

  constructor(private httpClient: HttpClient, private http: HttpClient, private config: EnvironmentConfigService) {
    this.pqcEndpoint = config.env.serverApiURL + '/v1/pqc';
    this.pqcTestType = `${this.pqcEndpoint}/test-types`;
    this.pqcTestField = `${this.pqcEndpoint}/test-fields`;
    this.pqcBatch = `${this.pqcEndpoint}/batches`;
    this.pqcTestResult = `${this.pqcEndpoint}/test-results`;
    this.pqcMeanTestResult = `${this.pqcEndpoint}/mean-test-results`;
    this.pqcDoubleBlindReview = `${this.pqcEndpoint}/test-results/double-blind-review`;
    this.sampleEndpoint = `${this.pqcEndpoint}/sample-transactions`;
  }

  //#region TestType

  public getAllTestTypes(criteria?: { [key: string]: string | string[] }): Observable<HttpResponse<PqcTestTypeDto[]>> {
    const params = {
      size: '1000',
      ...criteria,
    };
    return this.http
      .get<PqcTestTypeDto[]>(this.pqcTestType, { observe: 'response', params: params })
      .pipe(catchError(this.errorHandler));
  }

  public getTestTypeById(id: number): Observable<HttpResponse<PqcTestTypeDto>> {
    return this.http
      .get<PqcTestTypeDto>(this.pqcTestType + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Test Results

  public createTestResults(testResults: PqcTestResultDto[]): Observable<HttpResponse<PqcTestResultDto[]>> {
    return this.http
      .post<PqcTestResultDto[]>(`${this.pqcTestResult}/batch`, testResults, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createMeanTestResult(testResults: PqcMeanTestResultDto): Observable<HttpResponse<PqcMeanTestResultDto[]>> {
    return this.http
      .post<PqcMeanTestResultDto[]>(this.pqcMeanTestResult, testResults, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateTestResult(
    testResultId: number,
    testResult: PqcTestResultDto
  ): Observable<HttpResponse<PqcTestResultDto>> {
    return this.http
      .put<PqcTestResultDto>(`${this.pqcTestResult}/${testResultId}`, testResult, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getTestResultsByCriteria(criteria?: {
    [key: string]: string | string[];
  }): Observable<HttpResponse<PqcTestResultDto[]>> {
    return this.http
      .get<PqcTestResultDetailsDto[]>(`${this.pqcTestResult}/search`, {
        observe: 'response',
        params: {
          ...criteria,
        },
      })
      .pipe(catchError(this.errorHandler));
  }

  public reviewTestResult(testResultId: number): Observable<HttpResponse<PqcTestResultDto>> {
    return this.http
      .patch<PqcTestResultDto>(`${this.pqcTestResult}/${testResultId}/review`, null, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public doubleBlindReviewTestResult(
    testResults: PqcTestResultDto[]
  ): Observable<HttpResponse<DoubleBlindResponseDto>> {
    return this.http
      .post<DoubleBlindResponseDto>(this.pqcDoubleBlindReview, testResults, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public calculateTestResult(calculateRequest: PqcTestCalculationRequest): Observable<PqcTestCalculationResponse> {
    return this.http
      .post<PqcTestCalculationResponse>(`${this.pqcTestResult}/calculate`, calculateRequest)
      .pipe(catchError(this.errorHandler));
  }

  public searchTestResultsDetailsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<PqcTestResultDetailsDto[]>> {
    return this.http
      .get<PqcTestResultDetailsDto[]>(`${this.pqcTestResult}/search`, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Batch

  public getBatchesByCriteria(criteria?: object): Observable<HttpResponse<PqcBatchDto[]>> {
    return this.http
      .get<PqcBatchDto[]>(this.pqcBatch, {
        observe: 'response',
        params: { ...criteria, size: '3', sort: 'createDate,DESC' },
      })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Test Field

  public getTestFieldsByCriteria(criteria?: object): Observable<HttpResponse<PqcTestFieldDto[]>> {
    return this.http
      .get<PqcTestFieldDto[]>(this.pqcTestField, { observe: 'response', params: { ...criteria } })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Sample

  public submitSampleVerification(request: PqcSampleTransactionDto): Observable<HttpResponse<TransactionResponseDto>> {
    return this.httpClient
      .post<TransactionResponseDto>(this.sampleEndpoint, request, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  private errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
