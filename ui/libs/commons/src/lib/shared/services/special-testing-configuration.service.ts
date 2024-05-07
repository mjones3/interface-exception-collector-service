import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  AntibodyBatchDto,
  AntibodyTestDto,
  AntibodyTestTypeDto,
  AntibodyTestTypeEntryDto,
  HlaHpaAntigenEntryDTO,
  HlaHpaAntigenEntryInactivateDTO,
  HlaHpaAntigenReportDTO,
  HlaHpaAntigenTypeDTO,
  HlaHpaAntigenTypeEntryDTO,
  PreviousAntibodyTestResultDto,
  SpecialTestingConfigurationCriteriaDto,
  SpecialTestingConfigurationDto,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<SpecialTestingConfigurationCriteriaDto[]>;
type EntityArraySpecialTestingConfigurationResponseType = HttpResponse<SpecialTestingConfigurationDto[]>;
type HlaHpaAntigenEntryResponse = HttpResponse<HlaHpaAntigenEntryDTO>;
type HlaHpaAntigenEntriesResponse = HttpResponse<HlaHpaAntigenEntryDTO[]>;
type HlaHpaAntigenReportResponse = HttpResponse<HlaHpaAntigenReportDTO[]>;
type HlaHpaAntigenTypesResponse = HttpResponse<HlaHpaAntigenTypeDTO[]>;
type HlaHpaAntigenTypeEntriesResponse = HttpResponse<HlaHpaAntigenTypeEntryDTO[]>;

@Injectable({
  providedIn: 'root',
})
export class SpecialTestingConfigurationService {
  stccEndpoint: string;
  stcEndpoint: string;
  antibodyBatchEndpoint: string;
  antibodyTestEndpoint: string;
  antibodyTestTypeEndpoint: string;
  antibodyTestTypeEntryEndpoint: string;
  previousAntibodyTestResultEndpoint: string;
  hlaHpaEntriesEndpoint: string;
  hlaHpaReportEndpoint: string;
  hlaHpaAntigenTypesEndpoint: string;
  hlaHpaTypeEntriesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.stcEndpoint = config.env.serverApiURL + '/v1/special-testing-configurations';
    this.stccEndpoint = config.env.serverApiURL + '/v1/special-testing-configuration-criterias';
    this.antibodyTestEndpoint = config.env.serverApiURL + '/v1/antibody-tests';
    this.antibodyTestTypeEndpoint = config.env.serverApiURL + '/v1/antibody-test-types';
    this.antibodyTestTypeEntryEndpoint = config.env.serverApiURL + '/v1/antibody-test-type-entries';
    this.previousAntibodyTestResultEndpoint = config.env.serverApiURL + '/v1/previous-antibody-test-results';
    this.antibodyBatchEndpoint = config.env.serverApiURL + '/v1/antibody-batches';
    this.hlaHpaEntriesEndpoint = config.env.serverApiURL + '/v1/hla-hpa-antigen-entries';
    this.hlaHpaAntigenTypesEndpoint = config.env.serverApiURL + '/v1/hla-hpa-antigen-types';
    this.hlaHpaReportEndpoint = config.env.serverApiURL + '/v1/hla-hpa-antigen-entries/report';
    this.hlaHpaTypeEntriesEndpoint = config.env.serverApiURL + '/v1/hla-hpa-antigen-type-entries';
  }

  public insertSpecialTestingConfiguration(specialTestingConfig: SpecialTestingConfigurationDto) {
    return this.httpClient
      .post<SpecialTestingConfigurationCriteriaDto>(this.stcEndpoint, specialTestingConfig, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateSpecialTestingConfiguration(id: number, specialTestingConfig: SpecialTestingConfigurationDto) {
    return this.httpClient
      .put<SpecialTestingConfigurationCriteriaDto>(`${this.stcEndpoint}/${id}`, specialTestingConfig, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAllSpecialTestingConfigurationCriterias(): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<SpecialTestingConfigurationCriteriaDto[]>(this.stccEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getSpecialTestingConfiguration(criteria: {}): Observable<EntityArraySpecialTestingConfigurationResponseType> {
    return this.httpClient
      .get(this.stcEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //#region ANTIBODY TESTS

  public createAntibodyTest(antibodyTest: AntibodyTestDto): Observable<AntibodyTestDto> {
    return this.httpClient
      .post<AntibodyTestDto>(this.antibodyTestEndpoint, antibodyTest)
      .pipe(catchError(this.errorHandler));
  }

  public createAntibodyTests(antibodyTests: AntibodyTestDto[]): Observable<AntibodyTestDto[]> {
    return this.httpClient
      .post<AntibodyTestDto[]>(`${this.antibodyTestEndpoint}/batch`, antibodyTests)
      .pipe(catchError(this.errorHandler));
  }

  public updateAntibodyTest(id: number, antibodyTest: AntibodyTestDto): Observable<HttpResponse<AntibodyTestDto>> {
    return this.httpClient
      .put<AntibodyTestDto>(`${this.antibodyTestEndpoint}/${id}`, antibodyTest, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getAntibodyTestsByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<AntibodyTestDto[]>> {
    return this.httpClient
      .get<AntibodyTestDto[]>(this.antibodyTestEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAntibodyTestTypeByCriteria(criteria?: { [key: string]: any }): Observable<AntibodyTestTypeDto[]> {
    return this.httpClient
      .get<AntibodyTestTypeDto[]>(this.antibodyTestTypeEndpoint, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getAntibodyTestTypeEntriesByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<AntibodyTestTypeEntryDto[]> {
    return this.httpClient
      .get<AntibodyTestTypeEntryDto[]>(this.antibodyTestTypeEntryEndpoint, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getPreviousAntibodyTestResultsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<PreviousAntibodyTestResultDto[]> {
    return this.httpClient
      .get<PreviousAntibodyTestResultDto[]>(this.previousAntibodyTestResultEndpoint, {
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getPreviousAntibodyTestResultsByUnitNumber(unitNumber: string): Observable<PreviousAntibodyTestResultDto[]> {
    return this.httpClient
      .get<PreviousAntibodyTestResultDto[]>(this.previousAntibodyTestResultEndpoint + '/' + unitNumber)
      .pipe(catchError(this.errorHandler));
  }

  public getBatchById(id: number): Observable<HttpResponse<AntibodyBatchDto>> {
    return this.httpClient
      .get<AntibodyBatchDto>(this.antibodyBatchEndpoint + '/' + id, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getBatchesByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<AntibodyBatchDto[]>> {
    return this.httpClient
      .get<AntibodyBatchDto[]>(this.antibodyBatchEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createBatch(batches: AntibodyBatchDto): Observable<HttpResponse<AntibodyBatchDto>> {
    return this.httpClient
      .post<AntibodyBatchDto>(this.antibodyBatchEndpoint, batches, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public reviewAntibodyTest(id: number, antibodyTest: AntibodyTestDto): Observable<HttpResponse<AntibodyTestDto>> {
    return this.httpClient
      .put<AntibodyTestDto>(`${this.antibodyTestEndpoint}/${id}/review`, antibodyTest, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }
  //#endregion

  //#region HLA & HPA Entries
  findHlaHpaReportByCriteria(filter?: { [key: string]: any }): Observable<HlaHpaAntigenReportResponse> {
    return this.httpClient
      .get<HlaHpaAntigenReportResponse>(this.hlaHpaReportEndpoint, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  findHlaHpaEntriesByCriteria(filter?: { [key: string]: any }): Observable<HlaHpaAntigenEntriesResponse> {
    return this.httpClient
      .get<HlaHpaAntigenEntriesResponse>(this.hlaHpaEntriesEndpoint, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  findHlaHpaEntriesById(id: number): Observable<HlaHpaAntigenEntryResponse> {
    return this.httpClient
      .get<HlaHpaAntigenEntryResponse>(`${this.hlaHpaEntriesEndpoint}/${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  findHlaHpaAntigenTypesByCriteria(filter?: { [key: string]: any }): Observable<HlaHpaAntigenTypesResponse> {
    return this.httpClient
      .get<HlaHpaAntigenTypesResponse>(this.hlaHpaAntigenTypesEndpoint, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  findHlaHpaAntigenTypeEntriesByCriteria(filter?: {
    [key: string]: any;
  }): Observable<HlaHpaAntigenTypeEntriesResponse> {
    return this.httpClient
      .get<HlaHpaAntigenTypeEntriesResponse>(this.hlaHpaTypeEntriesEndpoint, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public updateHlaHpaEntry(id: number, hlaHpaEntry: HlaHpaAntigenEntryDTO): Observable<HlaHpaAntigenEntryResponse> {
    return this.httpClient
      .put<HlaHpaAntigenEntryResponse>(`${this.hlaHpaEntriesEndpoint}/${id}`, hlaHpaEntry, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public inactivateHlaHpaEntry(
    id: number,
    hlaHpaEntry: HlaHpaAntigenEntryInactivateDTO
  ): Observable<HlaHpaAntigenEntryResponse> {
    return this.httpClient
      .put<HlaHpaAntigenEntryResponse>(`${this.hlaHpaEntriesEndpoint}/${id}/inactivate`, hlaHpaEntry, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public reviewHlaHpaEntry(id: number, hlaHpaEntry: HlaHpaAntigenEntryDTO): Observable<HlaHpaAntigenEntryResponse> {
    return this.httpClient
      .put<HlaHpaAntigenEntryResponse>(`${this.hlaHpaEntriesEndpoint}/${id}/review`, hlaHpaEntry, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }
  //#endregion

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
