import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';
import { Pageable, PlateletsBacterialTestingDTO, PlateletsCADTestingDTO, PlateletsWorkloadDTO } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type PlateletWorkloadReportResponse = HttpResponse<Array<PlateletsWorkloadDTO>>;
type PlateletBacterialTestingReportResponse = HttpResponse<Array<PlateletsBacterialTestingDTO>>;
type PlateletCADTestingReportResponse = HttpResponse<Array<PlateletsCADTestingDTO>>;

@Injectable({
  providedIn: 'root',
})
export class PlateletService {
  private BASE_URI = '/v1/platelets';
  private BASE_URL = '';

  private targetWeightCalculationEndpoint: string;
  private apheresisPlateletsWorkloadReportEndpoint: string;
  private apheresisPlateletsBactrialTestingReportEndpoint: string;
  private apheresisPlateletsCADTestingReportEndpoint: string;

  constructor(private httpClient: HttpClient, config: EnvironmentConfigService) {
    this.BASE_URL = config.env.serverApiURL + this.BASE_URI;
    this.targetWeightCalculationEndpoint = this.BASE_URL + '/target-weight-calculations/calculate';
    this.apheresisPlateletsWorkloadReportEndpoint = this.BASE_URL + '/workload-report';
    this.apheresisPlateletsBactrialTestingReportEndpoint = this.BASE_URL + '/bacterial-testing-time-report';
    this.apheresisPlateletsCADTestingReportEndpoint = this.BASE_URL + '/cad-processing-time-report';
  }

  getTargetWeight(calculationRequest: any): Observable<any> {
    return this.httpClient
      .post<any>(this.targetWeightCalculationEndpoint, calculationRequest, { observe: 'response' })
      .pipe(map(value => value.body[0]))
      .pipe(catchError(this.errorHandler));
  }

  getWorkloadReport(clientReqParams: any, pageable: Pageable): Observable<PlateletWorkloadReportResponse> {
    return this.httpClient.get<Array<PlateletsWorkloadDTO>>(this.apheresisPlateletsWorkloadReportEndpoint, {
      params: this.getReportParams(clientReqParams, pageable),
      observe: 'response',
    });
  }

  getBacterialTestingReport(
    clientReqParams: any,
    pageable: Pageable
  ): Observable<PlateletBacterialTestingReportResponse> {
    return this.httpClient.get<Array<PlateletsBacterialTestingDTO>>(
      this.apheresisPlateletsBactrialTestingReportEndpoint,
      {
        params: this.getReportParams(clientReqParams, pageable),
        observe: 'response',
      }
    );
  }

  getCADTestingReport(clientReqParams: any, pageable: Pageable): Observable<PlateletCADTestingReportResponse> {
    return this.httpClient.get<Array<PlateletsCADTestingDTO>>(this.apheresisPlateletsCADTestingReportEndpoint, {
      params: this.getReportParams(clientReqParams, pageable),
      observe: 'response',
    });
  }

  private getReportParams(clientReqParams: any, pageable: Pageable): any {
    const params = {
      ...clientReqParams,
      ...pageable,
    };
    return params;
  }

  private errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
