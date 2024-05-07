import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { isArray } from 'lodash-es';
import { ToastrService } from 'ngx-toastr';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { TranslateInterpolationPipe } from '../../pipes/translate-interpolation.pipe';
import { StorageConfigurationDto, ValidateRuleDto } from '../models';
import { StorageDto, StorageProcessTypeDto } from '../models/storage.dto';
import { DeviceTypesService } from './device-types.service';
import { EnvironmentConfigService } from './environment-config.service';
import { FacilityService } from './facility.service';
import { InventoryService } from './inventory.service';

type StorageConfigurationResponse = HttpResponse<StorageConfigurationDto>;

@Injectable({
  providedIn: 'root',
})
export class StorageService {
  dataFound: boolean;
  storageEndpoint: string;
  storageProcessTypeEndpoint: string;
  storageConfigurationEndpoint: string;

  constructor(
    private facilityService: FacilityService,
    private inventoryService: InventoryService,
    private toaster: ToastrService,
    private httpClient: HttpClient,
    private config: EnvironmentConfigService,
    private translateService: TranslateService,
    private translateInterpolationPipe: TranslateInterpolationPipe,
    private deviceTypeService: DeviceTypesService
  ) {
    this.storageEndpoint = config.env.serverApiURL + '/v1/storages';
    this.storageProcessTypeEndpoint = config.env.serverApiURL + '/v1/storage-process-types';
    this.storageConfigurationEndpoint = config.env.serverApiURL + '/v1/storage-configurations';
  }

  async validateBarcode(barcodeToValidate: string, deviceTypeId: number[], processName: string): Promise<boolean> {
    // Todo: place logic that will be called from all storage components

    const facilityId = this.facilityService.getFacilityId();
    const dto = this.prepareValidateDto(
      barcodeToValidate,
      deviceTypeId,
      facilityId,
      'storage-barcode-and-type-eligibility-rule'
    );
    return new Promise((resolve, reject) => {
      this.inventoryService.validate(dto).subscribe(
        response => {
          const data = response.body;
          if (data.results !== '' && 'validDevice' in data.results) {
            const device = data.results.validDevice[0];
            this.dataFound = true;
          } else {
            const condition = data.results.condition[0];
            const errors = data.notifications[0];
            let device = '';
            this.deviceTypeService.getDeviceType(deviceTypeId[0]).subscribe(res => {
              device = res.body.descriptionKey;
            });
            //const device = this.translateService.instant(description);
            const processName1 = this.translateService.instant(processName);
            const interpolationObject = {
              device: device,
              barcodeToValidate: barcodeToValidate,
              processName: processName1,
            };

            if (condition === 1) {
              this.toaster.error(this.translateInterpolationPipe.transform(errors.message, interpolationObject));
            } else if (condition === 2) {
              this.toaster.error(this.translateInterpolationPipe.transform(errors.message, interpolationObject));
            } else {
              this.toaster.error('Invalid Barcode - None of the conditions matched.');
            }
            //this.toaster.error(errors.message);
            this.dataFound = false;
          }

          resolve(this.dataFound);
        },
        error => {
          this.toaster.error('error-getting-data.label');
          resolve(this.dataFound);
        }
      );
    });
  }

  prepareValidateDto(
    barcodeToValidate: string,
    typeId: number[],
    facilityId: number,
    ruleName: string
  ): ValidateRuleDto {
    const dto: ValidateRuleDto = {
      ruleName: ruleName,
      barcode: barcodeToValidate,
      typeIds: typeId,
      facilityId: facilityId,
    };
    return dto;
  }

  // public getStorageProcessTypeByDescriptionKey(
  //   storageProcessType: string
  // ): Observable<StorageProcessTypeResponseDto[]> {
  //   const params = { descriptionKey: storageProcessType };
  //   return this.httpClient
  //     .get<StorageProcessTypeResponseDto[]>(this.storageProcessTypeEndpoint, { observe: 'response', params: params })
  //     .pipe(map(response => response.body))
  //     .pipe(catchError(this.errorHandler));
  // }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  appendBloodTypeToProduct(results: any, selectedProduct: any) {
    selectedProduct = {
      ...selectedProduct,
      shortDescriptionKey: results?.iconData?.length ? results.iconData[0].shortDescriptionKey : '',
    };
    if ('isPooled' in results.validProduct[0].properties) {
      const validDonor = isArray(results.validProduct) ? results.validProduct[0] : {};
      return {
        ...selectedProduct,
        abo: validDonor.properties.ABO,
        rh: validDonor.properties.RH ? validDonor.properties.RH : '',
      };
    } else if ('validDonor' in results) {
      if (isArray(results.validDonor) && results.validDonor[0].abo) {
        return { ...selectedProduct, abo: results.validDonor[0].abo, rh: results.validDonor[0].rh };
      }
    }
    return { ...selectedProduct, abo: '', rh: '' };
  }

  appendPrtKitDescriptionToProduct(results: any, selectedProduct) {
    return {
      ...selectedProduct,
      prtKitDescription: selectedProduct?.properties['PRT_KIT'] ? selectedProduct?.properties['PRT_KIT'] : '',
    };
  }

  //#region Storage Process Type

  public getStorageProcessTypeByParam(criteria?: object): Observable<StorageProcessTypeDto[]> {
    return this.httpClient
      .get<StorageProcessTypeDto[]>(this.storageProcessTypeEndpoint, { params: { ...criteria } })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Storage

  public createStorage(dto: StorageDto): Observable<HttpResponse<StorageDto>> {
    return this.httpClient
      .post<StorageDto>(this.storageEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region Storage Configuration

  public createStorageConfiguration(dto: StorageConfigurationDto): Observable<HttpResponse<StorageConfigurationDto>> {
    return this.httpClient
      .post<StorageConfigurationDto>(this.storageConfigurationEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateStorageConfiguration(
    id: number,
    dto: StorageConfigurationDto
  ): Observable<StorageConfigurationResponse> {
    const url = `${this.storageConfigurationEndpoint}/${id}`;
    return this.httpClient
      .put<StorageConfigurationDto>(url, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getStorageConfigurationByParams(criteria?: object): Observable<HttpResponse<StorageConfigurationDto[]>> {
    return this.httpClient
      .get<StorageConfigurationDto[]>(this.storageConfigurationEndpoint, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion
}
