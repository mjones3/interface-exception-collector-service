import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { toasterDefaultConfig } from '../../components/toaster/toaster-default.config';
import { ProcessProductDto, ProcessProductVersionDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';
import { PROCESS_CONFIGURATION, PROCESS_PRODUCT_VERSION } from './mocks/process-mock';

type processProductArrayResponseType = HttpResponse<ProcessProductDto[]>;
type processProductResponseType = HttpResponse<ProcessProductDto>;
type processProductVersionResponseType = HttpResponse<ProcessProductVersionDto>;

@Injectable({
  providedIn: 'root',
})
export class ProcessService {
  private _moduleConfigurations: ProcessProductDto;
  private _processUrl = 'v1/processes/products';
  private _process_version_Url = 'v1/processes/products-version';

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {}

  get moduleConfigurations(): ProcessProductDto {
    return this._moduleConfigurations;
  }

  get processUrl(): string {
    return `${this.config.env.serverApiURL}/${this._processUrl}`;
  }

  get processVersionUrl(): string {
    return `${this.config.env.serverApiURL}/${this._process_version_Url}`;
  }

  /**
   * Get all Processes configurations
   */
  getAllProcesses(): Observable<processProductArrayResponseType> {
    return this.httpClient
      .get<ProcessProductDto[]>(this.processUrl, { observe: 'response' })
      .pipe(catchError(this.handleError));
  }

  getProcessPropertyValue(id: string, propertyName: string): Observable<string> {
    return this.httpClient
      .get<string>(`${this.processUrl}/${id}/properties/${propertyName}`)
      .pipe(catchError(this.handleError));
  }

  /**
   * Get Process Configuration
   * @param uuid Process Id
   */
  getProcessConfiguration(uuid: string): Observable<processProductResponseType> {
    return of(PROCESS_CONFIGURATION);
    /*
    return this.httpClient
      .get<ProcessProductDto>(`${this.processUrl}/${uuid}`, { observe: 'response' })
      .pipe(catchError(this.handleError));
     */
  }

  /**
   * Get Process Product Version
   * @param uuid Product Id
   */
  getProcessProductVersion(uuid: string): Observable<processProductVersionResponseType> {
    return of(PROCESS_PRODUCT_VERSION);
    /*
    const params = new HttpParams().set('versionType', 'build_version');

    return this.httpClient
      .get<ProcessProductVersionDto>(`${this.processVersionUrl}/${uuid}`, {
        params: params,
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
     */
  }

  /**
   * Init the default configurations for a Process Module
   * @param processConfigurations Process Configurations from DB
   */
  initModuleConfigurations(processConfigurations: ProcessProductDto): void {
    this._moduleConfigurations = processConfigurations;

    // Setting Toaster Config
    toasterDefaultConfig.timeOut = processConfigurations.properties['message-timeout']
      ? parseInt(processConfigurations.properties['message-timeout'], 10)
      : toasterDefaultConfig.timeOut;
  }

  private handleError(err) {
    /**
     * Handle logic here
     **/
    return throwError(err.message);
  }
}
