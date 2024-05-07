import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  InactivateTestResultDto,
  ResolveTestResultExceptionDto,
  ResolveTestResultExceptionResponseDto,
  TestGroupDto,
  TestGroupResponseDto,
  TestResultDiscrepancyReportDto,
  TestResultDto,
  TestResultExceptionDto,
  TestResultExceptionResolvedDto,
  TestTypeDto,
  TestTypeEntryDto,
  TestTypeReviewType,
  TransactionResponseDto,
} from '../models';
import { TestResultExceptionUnderInvestigationByReferenceIdDto } from '../models/test-result-exception-under-investigation-by-reference-id.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<TestResultDto>;
type EntityArrayResponseType = HttpResponse<TestResultDto[]>;
type TestTypeEntityArrayResponseType = HttpResponse<TestTypeDto[]>;
type TestEntryTypeEntityArrayResponseType = HttpResponse<TestTypeEntryDto[]>;

@Injectable({
  providedIn: 'root',
})
export class TestResultService {
  testResultEndpoint: string;
  reviewTestResultEndpoint: string;
  testTypeForDonationEndpoint: string;
  testEntryTypesEndpoint: string;
  testTypesEndpoint: string;
  testResultExceptionEndpoint: string;
  testGroupsEndpoint: string;
  testGroupStandardEndpoint: string;
  testGroupConfirmatoryEndpoint: string;
  testGroupNonRoutineEndpoint: string;
  testResultDiscrepancyReportEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.testResultEndpoint = config.env.serverApiURL + '/v1/test-results';
    this.reviewTestResultEndpoint = this.testResultEndpoint + '/review';
    this.testTypeForDonationEndpoint = config.env.serverApiURL + '/v1/test-types-for-donation';
    this.testEntryTypesEndpoint = config.env.serverApiURL + '/v1/test-entry-types';
    this.testTypesEndpoint = config.env.serverApiURL + '/v1/test-types';
    this.testResultExceptionEndpoint = config.env.serverApiURL + '/v1/test-result-exceptions';
    this.testGroupsEndpoint = `${config.env.serverApiURL}/v1/test-groups`;
    this.testGroupStandardEndpoint = `${config.env.serverApiURL}/v1/test-group-standards`;
    this.testGroupConfirmatoryEndpoint = `${config.env.serverApiURL}/v1/test-group-confirmatories`;
    this.testGroupNonRoutineEndpoint = `${config.env.serverApiURL}/v1/test-group-non-routine`;
    this.testResultDiscrepancyReportEndpoint = `${this.testResultExceptionEndpoint}/discrepancy-report`;
  }

  //#region TEST RESULT

  public addTestTesult(dto: TestResultDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<TestResultDto>(this.testResultEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateTestResult(dto: TestResultDto): Observable<EntityResponseType> {
    return this.httpClient
      .put<TestResultDto>(this.testResultEndpoint + '/' + dto.id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteTestResultById(id: number, comments: string): Observable<EntityResponseType> {
    return this.httpClient
      .request<TestResultDto>('delete', this.testResultEndpoint + '/' + id, { body: { comments }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public invalidateTestResult(id: number, inactivateDto: InactivateTestResultDto): Observable<EntityResponseType> {
    return this.httpClient
      .request<TestResultDto>('put', `${this.testResultEndpoint}/${id}/inactivate`, {
        body: inactivateDto,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllTestResults(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<TestResultDto[]>(this.testResultEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllTestResultsByCriteria(criteria?: object): Observable<HttpResponse<TestResultDto[]>> {
    return this.httpClient
      .get<TestResultDto[]>(this.testResultEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getTestResultById(id: string): Observable<HttpResponse<TestResultDto>> {
    return this.httpClient
      .get<TestResultDto>(this.testResultEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region TEST TYPE

  public getTestTypeById(id: string): Observable<HttpResponse<TestTypeDto>> {
    return this.httpClient
      .get<TestTypeDto>(this.testTypesEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getTestTypesByUnitNumber(unitNumber: string): Observable<TestTypeEntityArrayResponseType> {
    return this.httpClient
      .get<TestTypeDto[]>(this.testTypeForDonationEndpoint + '/' + unitNumber, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllTestTypes(criteria?: { [key: string]: any }): Observable<HttpResponse<TestTypeDto[]>> {
    return this.httpClient
      .get<TestTypeDto[]>(this.testTypesEndpoint, {
        params: { page: '0', size: '1000', sort: 'orderNumber,ASC', ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getTestTypesByCriteria(criteria?: { [key: string]: any }): Observable<TestTypeDto[]> {
    return this.httpClient
      .get<TestTypeDto[]>(this.testTypesEndpoint, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region TEST ENTRY TYPE

  public getTestEntryTypesByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<TestEntryTypeEntityArrayResponseType> {
    return this.httpClient
      .get<TestTypeEntryDto[]>(this.testEntryTypesEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region TEST RESULT REVIEW

  public testResultReview(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    const params = {
      'deleteDate.specified': 'false',
      'reviewResult.specified': 'false',
      'type.notEquals': 'AUTOMATIC',
      reviewType: TestTypeReviewType.USER,
      ...criteria,
    };

    return this.httpClient
      .get<TestResultDto[]>(this.testResultEndpoint, {
        params: { ...params },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public reviewTestResult(dto: TestResultDto): Observable<EntityResponseType> {
    return this.httpClient.put<TestResultDto>(this.reviewTestResultEndpoint + '/' + dto.id, dto, {
      observe: 'response',
    });
  }

  //#endregion

  //#region TEST RESULT EXCEPTION

  public getTestResultExceptionByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<TestResultExceptionDto[]>> {
    return this.httpClient
      .get<TestResultExceptionDto[]>(this.testResultExceptionEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public resolveAboRhException(
    id: number,
    dto: ResolveTestResultExceptionDto
  ): Observable<ResolveTestResultExceptionResponseDto> {
    return this.httpClient
      .put<ResolveTestResultExceptionResponseDto>(`${this.testResultExceptionEndpoint}/${id}/resolve`, dto)
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region TEST GROUP

  public getStandardTestsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<TestGroupResponseDto[]>> {
    return this.httpClient
      .get<TestGroupResponseDto[]>(this.testGroupStandardEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getConfirmatoryTestsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<TestGroupResponseDto[]>> {
    return this.httpClient
      .get<TestGroupResponseDto[]>(this.testGroupConfirmatoryEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getNonRoutineTestsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<TestGroupResponseDto[]>> {
    return this.httpClient
      .get<TestGroupResponseDto[]>(this.testGroupNonRoutineEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getLookbackTestGroupsByCriteria(criteria?: { [key: string]: any }): Observable<TestGroupDto[]> {
    return this.httpClient
      .get<TestGroupDto[]>(`${this.testGroupsEndpoint}`, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region TEST RESULT DISCREPANCY

  public getTestResultDiscrepancyReportByCriteria(
    criteria?: object
  ): Observable<HttpResponse<TestResultDiscrepancyReportDto[]>> {
    return this.httpClient
      .get<TestResultDiscrepancyReportDto[]>(this.testResultDiscrepancyReportEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public resolveTestResultDiscrepancy(
    request: TestResultExceptionResolvedDto,
    referenceId: number
  ): Observable<HttpResponse<TransactionResponseDto>> {
    return this.httpClient.post<TransactionResponseDto>(
      `${this.testResultExceptionEndpoint}/by-reference-id/${referenceId}/resolve-discrepancy`,
      request,
      { observe: 'response' }
    );
  }

  public testResultDiscrepancyUnderInvestigation(
    request: TestResultExceptionUnderInvestigationByReferenceIdDto
  ): Observable<HttpResponse<TransactionResponseDto>> {
    return this.httpClient.post<TransactionResponseDto>(
      `${this.testResultExceptionEndpoint}/under-investigation/transaction`,
      request,
      { observe: 'response' }
    );
  }

  //#endregion

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
