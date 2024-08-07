import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ProcessProductVersionModel } from '../models';
import { ProcessProductModel } from '../models/process-product.model';
import { EnvironmentConfigService } from './environment-config.service';

type processProductResponseType = HttpResponse<ProcessProductModel>;
type processProductVersionResponseType = HttpResponse<ProcessProductVersionModel>;

@Injectable({
  providedIn: 'root',
})
export class ProcessService {
  private _moduleConfigurations: ProcessProductModel;
  private _processUrl = 'v1/processes/products';
  private _process_version_Url = 'v1/processes/products-version';

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {}

  get moduleConfigurations(): ProcessProductModel {
    return this._moduleConfigurations;
  }

  get processUrl(): string {
    return `${this.config.env.serverApiURL}/${this._processUrl}`;
  }

  get processVersionUrl(): string {
    return `${this.config.env.serverApiURL}/${this._process_version_Url}`;
  }

  /**
   * Get Process Configuration
   * @param uuid Process Id
   */
  getProcessConfiguration(uuid: string): Observable<processProductResponseType> {
    return this.httpClient
      .get<ProcessProductModel>(`${this.processUrl}/${uuid}`, { observe: 'response' })
      .pipe(catchError(this.handleError));
  }

  /**
   * Get Process Product Version
   * @param uuid Product Id
   */
  getProcessProductVersion(uuid: string): Observable<processProductVersionResponseType> {
    const params = new HttpParams().set('versionType', 'build_version');

    return this.httpClient
      .get<ProcessProductVersionModel>(`${this.processVersionUrl}/${uuid}`, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }


  private handleError(err) {
    /**
     * Handle logic here
     **/
    return throwError(err.message);
  }
}
