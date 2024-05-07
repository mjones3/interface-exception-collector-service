import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  DonorAlert,
  DonorNotificationDto,
  DonorNotificationHistoryDto,
  DonorNotificationOnDemandDto,
  NotificationFormDto,
  NotificationFormOptionsDto,
  NotificationReportDto,
  PendingNotificationDto,
  PrintDto,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseTypeCouns = HttpResponse<DonorNotificationOnDemandDto>;
type EntityResponseTypeNotification = HttpResponse<DonorNotificationDto>;
@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  notificationFormsEndpoint: string;
  donorReportEndpoint: string;
  donorNotificationEndpoint: string;
  donorNotificationBatchEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.notificationFormsEndpoint = config.env.serverApiURL + '/v1/notification-forms';
    this.donorReportEndpoint = config.env.serverApiURL + '/v1/donor-notifications/report';
    this.donorNotificationBatchEndpoint = config.env.serverApiURL + '/v1/donor-notifications/batch';
    this.donorNotificationEndpoint = config.env.serverApiURL + '/v1/donor-notifications';
  }

  /**
   * get Donor Report with criteria
   */
  public getDonorReportByCriteria(criteria: { [key: string]: any }): Observable<HttpResponse<DonorNotificationDto[]>> {
    return this.httpClient
      .get<DonorNotificationDto[]>(this.donorReportEndpoint, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getNotificationsForms(): Observable<HttpResponse<NotificationFormOptionsDto[]>> {
    return this.httpClient
      .get<NotificationFormOptionsDto>(this.notificationFormsEndpoint, {
        params: { size: '1000' },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorNotification(id: number): Observable<HttpResponse<PendingNotificationDto>> {
    return this.httpClient
      .get<PendingNotificationDto>(this.donorNotificationEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public exportPendingNotificationsByCriteria(dto: NotificationReportDto): Observable<HttpResponse<PrintDto>> {
    return this.httpClient
      .post<PrintDto>(this.donorNotificationEndpoint + '/exports', dto)
      .pipe(catchError(this.errorHandler));
  }

  public exportNotificationsByCriteria(dto: NotificationReportDto): Observable<HttpResponse<PrintDto>> {
    return this.httpClient
      .post<PrintDto>(this.donorNotificationEndpoint + '/exports/print', dto)
      .pipe(catchError(this.errorHandler));
  }

  public updateDonorNotification(dto: PendingNotificationDto): Observable<HttpResponse<NotificationFormDto>> {
    return this.httpClient
      .put<NotificationFormDto>(this.donorNotificationEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorNotificationExportHistory(
    params,
    pageable?,
    sortInfo?
  ): Observable<HttpResponse<DonorNotificationHistoryDto[]>> {
    return this.httpClient
      .get<DonorNotificationHistoryDto[]>(this.donorReportEndpoint, {
        params: { ...pageable, ...params, ...sortInfo },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public createCounseling(dto: DonorNotificationOnDemandDto): Observable<EntityResponseTypeCouns> {
    return this.httpClient
      .post<DonorNotificationOnDemandDto>(this.donorNotificationBatchEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateCounseling(dto: DonorNotificationDto): Observable<EntityResponseTypeCouns> {
    return this.httpClient
      .put<DonorNotificationDto>(this.donorNotificationEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createNotification(dto: DonorNotificationOnDemandDto): Observable<EntityResponseTypeNotification> {
    return this.httpClient
      .post<DonorNotificationOnDemandDto>(this.donorNotificationBatchEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorNotificationAlerts(
    donorId: number,
    criteria?: { [param: string]: string }
  ): Observable<HttpResponse<DonorAlert>> {
    return this.httpClient
      .get<DonorAlert>(`${this.donorNotificationEndpoint}/${donorId}/count`, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  public getDonorNotificationsByCriteria(criteria?: object): Observable<HttpResponse<DonorNotificationDto[]>> {
    return this.httpClient
      .get<DonorNotificationDto[]>(`${this.donorNotificationEndpoint}`, {
        observe: 'response',
        params: { ...criteria },
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
