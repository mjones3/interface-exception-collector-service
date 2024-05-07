import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DeviceTypeDto, TestResultDto } from '../models';
import { ConversionMappingDto } from '../models/conversion-mapping.dto';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<ConversionMappingDto[]>;

@Injectable({
  providedIn: 'root',
})
export class ConversionMappingService {
  private resourceUrl: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.resourceUrl = config.env.serverApiURL + '/v1/conversion-mappings';
  }

  private handleError(err) {
    /**
     * Handle logic here
     **/
    return throwError(err.message);
  }

  /**
   * Get Reasons by criteria typeId and params
   * @param typeId
   * @param pageable
   */
  getConversionMappings(type: string, valueFrom: string): Observable<HttpResponse<ConversionMappingDto[]>> {
    const urlParams = valueFrom ? '&valueFrom=' + valueFrom : '';
    return this.httpClient
      .get<ConversionMappingDto[]>(this.resourceUrl + '?type=' + type + urlParams, {
        //params: { sort: 'descriptionKey,ASC' },
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get Reasons by criteria typeId and params
   * @param criteria
   */
  public getAllConversionTypeMappingsByCriteria(criteria?: {
    [key: string]: any;
  }): Observable<HttpResponse<ConversionMappingDto[]>> {
    return this.httpClient
      .get<ConversionMappingDto[]>(this.resourceUrl, {
        params: { ...criteria },
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }
}
