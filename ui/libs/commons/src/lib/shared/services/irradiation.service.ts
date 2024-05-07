import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ClosedBatchResponseDto } from '../models/closed-batch.dto';
import { IrradiationDto } from '../models/irradiation.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class IrradiationService {
  irradiationEndpoint: string;
  closedIrradiationEndpoint: string;

  actualFullISBTCode: string;
  actualUnitlNumber: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.irradiationEndpoint = config.env.serverApiURL + '/v1/irradiations';
    this.closedIrradiationEndpoint = config.env.serverApiURL + '/v1/closed-irradiations';
  }

  getIrradiationRuleName() {
    return 'rul-0086-add-validate-irradiation-batch-product';
  }

  getIrradiationRuleNameForSubmitInStartProcess() {
    return 'rul-0086-save-irradiation-batch';
  }

  getIrradiationRuleNameForSubmitInCloseProcess() {
    return 'rul-0086-close-irradiation-batch';
  }

  setFullISBTCode(fullISBTCode: string) {
    this.actualFullISBTCode = fullISBTCode;
  }

  setUnitNumber(unitNumber: string) {
    this.actualUnitlNumber = unitNumber;
  }

  getDescriptionForStatus() {
    return [
      {
        value: this.actualUnitlNumber,
        label: 'unit-number.label',
      },
      {
        value: this.actualFullISBTCode,
        label: 'product-code.label',
      },
    ];
  }

  getIrradiationsByLocation(locationId: number): Observable<HttpResponse<IrradiationDto[]>> {
    if (locationId) {
      return this.httpClient
        .get<IrradiationDto[]>(this.irradiationEndpoint, {
          params: {
            locationId: `${locationId}`,
            'closeDate.specified': 'false',
          },
          observe: 'response',
        })
        .pipe(catchError(this.errorHandler));
    }
  }

  getInventoriesByIrradiationId(id: number): Observable<HttpResponse<ClosedBatchResponseDto>> {
    if (id) {
      return this.httpClient
        .get<ClosedBatchResponseDto>(`${this.closedIrradiationEndpoint}/${id}`, { observe: 'response' })
        .pipe(catchError(this.errorHandler));
    }
  }

  getIrradiationsNotClose(description: string): Observable<HttpResponse<IrradiationDto[]>> {
    if (description) {
      return this.httpClient
        .get<IrradiationDto[]>(this.irradiationEndpoint, {
          params: {
            irradiator: description,
            'closeDate.specified': 'false',
          },
          observe: 'response',
        })
        .pipe(catchError(this.errorHandler));
    }
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
