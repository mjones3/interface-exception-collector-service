import { HttpClient, HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  ApproveSamplingPlanDto,
  CloneSamplingPlanDto,
  CreateSamplingPlanDto,
  CrossoverEligibilityRequestDto,
  CrossoverEligibilityResponseDto,
  EndSamplingPlanDto,
  EvaluationModeDto,
  ProductTestTypeDto,
  ProductTypeDto,
  RequestedSampleDto,
  ResolveSampleFailureDto,
  ReviewSamplingPlanDto,
  SampleFailureReviewDTO,
  SamplingPlanDto,
  SamplingPlanTestsDto,
  SaveCrossoverRequestDto,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class SamplingPlanService {
  samplingPlansEndpoint: string;
  samplingPlanEndpoint: string;
  productTypeEndpoint: string;
  productTestTypeEndpoint: string;
  requestedSamplesEndpoint: string;
  samplingPlanTestsEndpoint: string;
  samplingPlanEvaluationModesEndpoint: string;
  samplingPlanCrossoverEligible: string;
  samplingPlanCrossover: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.samplingPlansEndpoint = config.env.serverApiURL + '/v1/sampling-plans';
    this.samplingPlanEndpoint = config.env.serverApiURL + '/v1/sampling-plan';
    this.productTypeEndpoint = `${this.samplingPlanEndpoint}/product-types`;
    this.productTestTypeEndpoint = `${this.samplingPlanEndpoint}/product-test-types`;
    this.requestedSamplesEndpoint = `${config.env.serverApiURL}/v1/requested-samples`;
    this.samplingPlanTestsEndpoint = `${this.samplingPlanEndpoint}/tests`;
    this.samplingPlanEvaluationModesEndpoint = `${config.env.serverApiURL}/v1/reports/sampling-plan/evaluation-modes`;
    this.samplingPlanCrossover = `${this.samplingPlanEndpoint}/crossover`;
    this.samplingPlanCrossoverEligible = `${this.samplingPlanCrossover}/eligible`;
  }

  public getProductTestTypeByCriteria(criteria?: { [key: string]: any }): Observable<ProductTestTypeDto[]> {
    return this.httpClient
      .get<ProductTestTypeDto[]>(this.productTestTypeEndpoint, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getProductTypesByCriteria(criteria?: { [key: string]: any }): Observable<ProductTypeDto[]> {
    return this.httpClient
      .get<ProductTypeDto[]>(this.productTypeEndpoint, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getProductTypeById(id: string): Observable<HttpResponse<ProductTypeDto>> {
    return this.httpClient
      .get<ProductTypeDto>(`${this.productTypeEndpoint}/${id}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getSamplingPlanByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<SamplingPlanDto[]>> {
    return this.httpClient
      .get<SamplingPlanDto[]>(this.samplingPlansEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createSamplingPlan(dto: CreateSamplingPlanDto): Observable<CreateSamplingPlanDto> {
    return this.httpClient
      .post<CreateSamplingPlanDto>(this.samplingPlansEndpoint, dto)
      .pipe(catchError(this.errorHandler));
  }

  public updateSamplingPlan(id: number, dto: CreateSamplingPlanDto): Observable<CreateSamplingPlanDto> {
    return this.httpClient
      .put<CreateSamplingPlanDto>(`${this.samplingPlansEndpoint}/${id}`, dto)
      .pipe(catchError(this.errorHandler));
  }

  public cloneSamplingPlan(samplingPlanId: number, dto: CloneSamplingPlanDto): Observable<SamplingPlanDto> {
    return this.httpClient
      .post<SamplingPlanDto>(`${this.samplingPlanEndpoint}/${samplingPlanId}/clone`, dto)
      .pipe(catchError(this.errorHandler));
  }

  public endSamplingPlan(samplingPlanId: number, dto: EndSamplingPlanDto): Observable<SamplingPlanDto> {
    return this.httpClient
      .post<SamplingPlanDto>(`${this.samplingPlanEndpoint}/${samplingPlanId}/end/${dto.reasonKey}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public approveSamplingPlan(samplingPlanId: number, dto: ApproveSamplingPlanDto): Observable<SamplingPlanDto> {
    return this.httpClient
      .post<SamplingPlanDto>(`${this.samplingPlanEndpoint}/${samplingPlanId}/approve`, dto)
      .pipe(catchError(this.errorHandler));
  }

  public getRequestedSamplesByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<RequestedSampleDto[]>> {
    return this.httpClient
      .get<RequestedSampleDto[]>(`${this.samplingPlanEndpoint}/requested-samples`, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public resolveFailure(id: number, dto: ResolveSampleFailureDto): Observable<RequestedSampleDto> {
    return this.httpClient
      .put<RequestedSampleDto>(`${this.requestedSamplesEndpoint}/${id}/resolve`, dto)
      .pipe(catchError(this.errorHandler));
  }

  public reviewFailure(id: number, dto: SampleFailureReviewDTO): Observable<RequestedSampleDto> {
    return this.httpClient
      .put<RequestedSampleDto>(`${this.requestedSamplesEndpoint}/${id}/review`, dto)
      .pipe(catchError(this.errorHandler));
  }

  public getSamplingPlanById(samplingPlanId: number): Observable<HttpResponse<SamplingPlanDto>> {
    return this.httpClient
      .get<SamplingPlanDto[]>(`${this.samplingPlansEndpoint}/${samplingPlanId}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public reviewSamplingPlan(
    samplingPlanId: number,
    dto: ReviewSamplingPlanDto
  ): Observable<HttpResponse<SamplingPlanDto[]>> {
    return this.httpClient
      .post<SamplingPlanDto>(`${this.samplingPlanEndpoint}/${samplingPlanId}/review`, dto)
      .pipe(catchError(this.errorHandler));
  }

  public getSamplingPlanTestsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<SamplingPlanTestsDto[]>> {
    return this.httpClient
      .get<SamplingPlanTestsDto[]>(this.samplingPlanTestsEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getSamplePlanEvaluationModesByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<EvaluationModeDto[]>> {
    return this.httpClient
      .get<EvaluationModeDto[]>(this.samplingPlanEvaluationModesEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getSamplePlanCrossoverEligibility(
    criteria: CrossoverEligibilityRequestDto
  ): Observable<HttpResponse<CrossoverEligibilityResponseDto>> {
    let queryParams = new HttpParams();
    queryParams = queryParams.append('productTypeKey', criteria.productTypeKey);
    queryParams = queryParams.append('donationId', criteria.donationId.toString());
    queryParams = queryParams.append('inventoryId', criteria.inventoryId.toString());
    queryParams = queryParams.append('sampleTaskKey', criteria.sampleTaskKey);

    return this.httpClient
      .get<CrossoverEligibilityResponseDto>(this.samplingPlanCrossoverEligible, {
        params: queryParams,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createSamplingPlanCrossover(dto: SaveCrossoverRequestDto): Observable<HttpResponse<void>> {
    return this.httpClient
      .post<HttpResponse<void>>(this.samplingPlanCrossover, dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
