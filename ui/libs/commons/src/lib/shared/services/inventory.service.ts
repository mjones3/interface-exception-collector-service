import { HttpClient, HttpErrorResponse, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError, delay } from 'rxjs/operators';
import {
  BacterialSampleTimeReportDto,
  ExpirationDateDto,
  ImportDto,
  ImportStatusDto,
  InventoriesResponseDto,
  InventoryByCategoryDto,
  InventoryDto,
  InventoryHierarchyDto,
  InventoryParentUpdateDto,
  InventoryPropertyDto,
  InventoryQuarantineDto,
  InventoryResponseDto,
  InventoryUpdateDto,
  LotDto,
  PlasmaConversionDto,
  ProductStatusDto,
  RuleResponseDto,
  SeparationDto,
  ValidateRuleDto,
  VolumeDto,
} from '../models';
import { PooledPlateletsWorkloadReportDto } from '../models/pooled-platelets-workload-report.dto';
import { EnvironmentConfigService } from './environment-config.service';

type InventoryResponse = HttpResponse<InventoryResponseDto>;
type InventoriesResponse = HttpResponse<InventoriesResponseDto>;
type RulesResponse = HttpResponse<RuleResponseDto>;
type PooledPlateletsBacterialSampleTimeReportResponse = HttpResponse<BacterialSampleTimeReportDto[]>;
type PooledPlateletsWorkloadReportResponse = HttpResponse<PooledPlateletsWorkloadReportDto[]>;

declare var dT_: any;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class InventoryService {
  inventoryEndpoint: string;
  updateInventoryEndpoint: string;
  inventoryRuleValidationEndpoint: string;
  discardInventoryEndpoint: string;
  quarantineInventoryEndpoint: string;
  deleteInventoryEndpoint: string;
  separationInventoryEndpoint: string;
  addInventoryVolumeEndpoint: string;
  updateInventoryProcessEndpoint: string;
  updateInventoryStatusEndpoint: string;
  updateIndexAndStatusEndpoint: string;
  createPropertiesEndpoint: string;
  currentLotNumberEndpoint: string;
  createPlasmaConversionEndpoint: string;
  productCategoryEndpoint: string;
  ppBacterialSampleTimeReportEndpoint: string;
  pooledPlateletsWorkloadReportEndpoint: string;
  createInventoryAndUpdateParentInventoryEndpoint: string;

  //IMPORTS
  importsEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
    this.inventoryEndpoint = config.env.serverApiURL + '/v1/inventories';
    this.inventoryRuleValidationEndpoint = this.inventoryEndpoint + '/validate';
    this.discardInventoryEndpoint = this.inventoryEndpoint + '/{inventoryId}/discard';
    this.quarantineInventoryEndpoint = this.inventoryEndpoint + '/{inventoryId}/quarantine';
    this.createInventoryAndUpdateParentInventoryEndpoint = this.inventoryEndpoint + '/{inventoryId}/inventories';
    this.deleteInventoryEndpoint = this.inventoryEndpoint + '/{inventoryId}';
    this.updateInventoryEndpoint = this.inventoryEndpoint + '/{inventoryId}';
    this.separationInventoryEndpoint = this.inventoryEndpoint + '/{inventoryId}/separations';
    this.addInventoryVolumeEndpoint = this.inventoryEndpoint + '/{inventoryId}/volumes';
    this.updateInventoryProcessEndpoint = this.inventoryEndpoint + '/{inventoryId}/processes';
    this.updateInventoryStatusEndpoint = this.inventoryEndpoint + '/{inventoryId}/statuses';
    this.updateIndexAndStatusEndpoint = this.inventoryEndpoint + '/{inventoryId}/processes-statuses';
    this.createPropertiesEndpoint = this.inventoryEndpoint + '/{inventoryId}/properties';
    this.currentLotNumberEndpoint = this.inventoryEndpoint + '/{inventoryId}/current-lot-number';
    this.createPlasmaConversionEndpoint = this.inventoryEndpoint + '/create-plasma-conversion';
    this.productCategoryEndpoint = this.inventoryEndpoint + '/by-category';
    this.importsEndpoint = config.env.serverApiURL + '/v1/imports';
    this.ppBacterialSampleTimeReportEndpoint =
      config.env.serverApiURL + '/v1/pooled-platelets-bacterial-sample-time-reports';
    this.pooledPlateletsWorkloadReportEndpoint = config.env.serverApiURL + '/v1/pooled-platelets-workload-reports';
  }

  public getAllInventory(): Observable<HttpResponse<InventoryResponseDto[]>> {
    return this.httpClient
      .get<InventoryResponseDto>(this.inventoryEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getAllInventoryByDonationId(donationId: string): Observable<HttpResponse<InventoryResponseDto[]>> {
    return this.httpClient
      .get<InventoryResponseDto>(this.inventoryEndpoint, { params: { donationId }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getInventory(id: number): Observable<InventoryResponse> {
    return this.httpClient
      .get<InventoryResponseDto>(this.inventoryEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getInventoryByCategory(params): Observable<HttpResponse<InventoryByCategoryDto[]>> {
    return this.httpClient
      .get<InventoryByCategoryDto[]>(this.productCategoryEndpoint, {
        params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getInventoryByCriteria(params): Observable<HttpResponse<InventoryResponseDto[]>> {
    return this.httpClient
      .get<InventoryResponseDto[]>(this.inventoryEndpoint, {
        params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createInventory(dto: InventoryDto): Observable<InventoryResponse> {
    return this.httpClient
      .post<InventoryResponseDto>(this.inventoryEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createInventoryAndUpdateParentInventory(
    id: number,
    dto: InventoryParentUpdateDto
  ): Observable<InventoryResponse> {
    return this.httpClient
      .put<InventoryResponseDto>(
        this.replaceValueInUrl(this.createInventoryAndUpdateParentInventoryEndpoint, id),
        dto,
        { observe: 'response' }
      )
      .pipe(catchError(this.errorHandler));
  }

  public updateInventory(id: number, dto: InventoryUpdateDto): Observable<InventoryResponse> {
    return this.httpClient
      .put<InventoryResponseDto>(this.replaceValueInUrl(this.updateInventoryEndpoint, id), dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public discardInventory(id: number): Observable<InventoryResponse> {
    return this.httpClient
      .put<InventoryResponseDto>(this.replaceValueInUrl(this.discardInventoryEndpoint, id), null, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getQuarantineInventory(id: number): Observable<HttpResponse<InventoryQuarantineDto>> {
    return this.httpClient
      .get<InventoryResponseDto>(this.replaceValueInUrl(this.quarantineInventoryEndpoint, id), {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }
  public quarantineInventory(id: number): Observable<HttpResponse<InventoryResponse>> {
    return this.httpClient
      .put<InventoryResponseDto>(this.replaceValueInUrl(this.quarantineInventoryEndpoint, id), null, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public validateRulesFromInventories(dto: ValidateRuleDto): Observable<RulesResponse> {
    return this.httpClient
      .put<RuleResponseDto>(this.inventoryRuleValidationEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public validate(inputs: any): Observable<RulesResponse> {
    if (inputs.mock) {
      return of(inputs.mock).pipe(delay(1000));
    }

    return this.httpClient
      .put<RuleResponseDto>(this.inventoryRuleValidationEndpoint, inputs, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateInventoryProcess(id: number, processIndex: string): Observable<InventoryResponse> {
    //send status as param
    const params = new HttpParams().set('processIndex', processIndex);
    return this.httpClient
      .put<InventoryResponseDto>(this.replaceValueInUrl(this.updateInventoryProcessEndpoint, id), null, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public updateInventoryStatus(id: number, status: string): Observable<InventoryResponse> {
    const params = new HttpParams().set('status', status);
    return this.httpClient
      .put<InventoryResponseDto>(this.replaceValueInUrl(this.updateInventoryStatusEndpoint, id), null, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public inventorySeparation(id: number, dto: SeparationDto): Observable<InventoriesResponse> {
    return this.httpClient.post<InventoriesResponseDto>(
      this.replaceValueInUrl(this.separationInventoryEndpoint, id),
      dto,
      { observe: 'response' }
    );
  }

  public deleteInventory(id: number): Observable<InventoryResponse> {
    return this.httpClient
      .delete<InventoryResponseDto>(this.replaceValueInUrl(this.inventoryEndpoint, id), { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public isQuarantineInventory(id: number): Observable<HttpResponse<InventoryResponseDto>> {
    return this.httpClient
      .get<InventoryResponseDto>(this.replaceValueInUrl(this.quarantineInventoryEndpoint, id), {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public searchInventoryByFields(id: number, productCode: string): Observable<HttpResponse<InventoryResponseDto[]>> {
    //add params to search by fields
    const params = new HttpParams().set('donationId', String(id)).set('productCode', productCode);

    return this.httpClient
      .get<InventoryResponseDto[]>(this.inventoryEndpoint, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public updateInventoryVolume(id: number, dto: VolumeDto): Observable<InventoriesResponse> {
    return this.httpClient
      .post<InventoryResponseDto>(this.replaceValueInUrl(this.addInventoryVolumeEndpoint, id), dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  updateProcessIndexAndStatusOfInventory(id: number, processIndex: string, status: string) {
    const params = new HttpParams().set('processIndex', processIndex).set('status', status);
    return this.httpClient
      .put<InventoryResponseDto>(this.replaceValueInUrl(this.updateIndexAndStatusEndpoint, id), null, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public addProperties(
    id: number | string,
    dto: InventoryPropertyDto[]
  ): Observable<HttpResponse<Map<string, string>>> {
    return this.httpClient
      .post<Map<string, string>>(this.replaceValueInUrl(this.createPropertiesEndpoint, id), dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  getCurrentLotNumber(id: number): Observable<HttpResponse<LotDto>> {
    if (id) {
      return this.httpClient
        .get<HttpResponse<LotDto>>(this.replaceValueInUrl(this.currentLotNumberEndpoint, id), { observe: 'response' })
        .pipe(catchError(this.errorHandler));
    }
  }

  public createPlasmaConversion(dto: PlasmaConversionDto): Observable<HttpResponse<InventoryDto>> {
    return this.httpClient
      .post<InventoryDto>(this.createPlasmaConversionEndpoint, dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public calculateFutureExpirationDate(inventoryId: number): Observable<HttpResponse<ExpirationDateDto>> {
    return this.httpClient
      .get<HttpResponse<ExpirationDateDto>>(
        `${this.inventoryEndpoint}/${inventoryId}/calculate-future-expiration-date`,
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public checkStatus(inventoryIds: number[]): Observable<ProductStatusDto[]> {
    return this.httpClient
      .get<ProductStatusDto[]>(`${this.inventoryEndpoint}/status-check`, {
        params: { inventoryIds: inventoryIds.join(',') },
      })
      .pipe(catchError(this.errorHandler));
  }

  private replaceValueInUrl(url: string, value: any): string {
    return url.slice().replace('{inventoryId}', String(value));
  }

  //#region  IMPORTS

  public completeImport(dto: ImportDto): Observable<ImportDto> {
    return this.httpClient.post<ImportDto>(this.importsEndpoint, dto).pipe(catchError(this.errorHandler));
  }

  public getImportStatus(importId: number): Observable<ImportStatusDto> {
    return this.httpClient
      .get<ImportStatusDto>(`${this.importsEndpoint}/${importId}/status`)
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  public getPooledPlateletsBacterialSampleTimeReportByFilter(
    criteria,
    pageable
  ): Observable<PooledPlateletsBacterialSampleTimeReportResponse> {
    return this.httpClient
      .get<BacterialSampleTimeReportDto[]>(this.ppBacterialSampleTimeReportEndpoint, {
        params: { ...criteria, ...pageable },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getPooledPlateletsWorkloadReportByFilter(
    criteria,
    pageable
  ): Observable<PooledPlateletsWorkloadReportResponse> {
    return this.httpClient
      .get<PooledPlateletsWorkloadReportDto[]>(this.pooledPlateletsWorkloadReportEndpoint, {
        params: { ...criteria, ...pageable },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getInventoryHierarchy(inventoryId: number): Observable<HttpResponse<InventoryHierarchyDto[]>> {
    return this.httpClient
      .get<InventoryHierarchyDto[]>(`${this.inventoryEndpoint}/${inventoryId}/hierarchy`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
