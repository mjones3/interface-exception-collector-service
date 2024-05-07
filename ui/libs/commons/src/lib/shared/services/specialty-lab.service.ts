import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  HgbsKitEntryDTO,
  HgbsKitEntryReviewDTO,
  QcEntryDTO,
  QcEntryReviewDTO,
  QcMonthlyReviewDTO,
  QCReportResultsDto,
  ReagentDTO,
} from '../models';
import { QcReagentDTO } from '../models/qc-reagent';
import { EnvironmentConfigService } from './environment-config.service';

export type HgbsKitEntryResponse = HttpResponse<HgbsKitEntryDTO>;
export type QcEntryResponse = HttpResponse<QcEntryDTO>;
type SaveMonthlyReviewResponse = HttpResponse<QcMonthlyReviewDTO>;
type QcReportResultResponse = HttpResponse<QCReportResultsDto[]>;
type QcReagentResponse = HttpResponse<QcReagentDTO[]>;

@Injectable({
  providedIn: 'root',
})
export class SpecialtyLabService {
  private hgbsKitEntriesEndpoint: string;
  private qcEntriesEndpoint: string;
  private qcMonthlyReviewEndpoint: string;
  private reagentsEndpoint: string;

  constructor(private readonly httpClient: HttpClient, readonly config: EnvironmentConfigService) {
    this.hgbsKitEntriesEndpoint = config.env.serverApiURL + '/v1/hgbs-kit-entries';
    this.qcEntriesEndpoint = config.env.serverApiURL + '/v1/qc-entries';
    this.qcMonthlyReviewEndpoint = config.env.serverApiURL + '/v1/qc-monthly-reviews';
    this.reagentsEndpoint = config.env.serverApiURL + '/v1/reagents';
  }

  createHgbsEntry(hgbsKitEntryDTO: HgbsKitEntryDTO): Observable<HgbsKitEntryDTO> {
    return this.httpClient
      .post<HgbsKitEntryDTO>(this.hgbsKitEntriesEndpoint, hgbsKitEntryDTO)
      .pipe(catchError(this.errorHandler));
  }

  findHgbsEntryById(hgbsKitEntryId: number): Observable<HgbsKitEntryResponse> {
    return this.httpClient
      .get<HgbsKitEntryDTO[]>(this.hgbsKitEntriesEndpoint + '/' + hgbsKitEntryId, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  findReagents(filter?: { [key: string]: any }): Observable<QcReagentResponse> {
    return this.httpClient
      .get<QcReagentResponse>(this.reagentsEndpoint, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  findReagentId(id: number): Observable<HttpResponse<ReagentDTO>> {
    return this.httpClient
      .get<QcEntryDTO>(this.reagentsEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  createHgbsEntryReview(hgbsKitEntryReviewDTO: HgbsKitEntryReviewDTO): Observable<HgbsKitEntryResponse> {
    return this.httpClient
      .patch(`${this.hgbsKitEntriesEndpoint}/${hgbsKitEntryReviewDTO.hgbsKitId}/review`, hgbsKitEntryReviewDTO)
      .pipe(catchError(this.errorHandler));
  }

  getHgbsEntryReviewReports(filter: any): Observable<QcReportResultResponse> {
    return this.httpClient
      .get(`${this.hgbsKitEntriesEndpoint}/review-report`, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  createQcEntry(qcEntryDto: QcEntryDTO): Observable<QcEntryDTO> {
    return this.httpClient.post<QcEntryDTO>(this.qcEntriesEndpoint, qcEntryDto).pipe(catchError(this.errorHandler));
  }

  findQcEntryById(qcEntryId: number): Observable<QcEntryResponse> {
    return this.httpClient
      .get<Array<QcEntryDTO>>(`${this.qcEntriesEndpoint}/${qcEntryId}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  createQcEntryReview(qcEntryReviewDTO: QcEntryReviewDTO): Observable<QcEntryResponse> {
    return this.httpClient
      .patch(`${this.qcEntriesEndpoint}/${qcEntryReviewDTO.qcEntryId}/review`, qcEntryReviewDTO)
      .pipe(catchError(this.errorHandler));
  }

  getQcEntryReviewReports(filter: any): Observable<QcReportResultResponse> {
    return this.httpClient
      .get<HgbsKitEntryDTO[]>(`${this.qcEntriesEndpoint}/review-report`, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  createMonthlyReview(qcMonthlyReview: QcMonthlyReviewDTO): Observable<QcMonthlyReviewDTO> {
    return this.httpClient
      .post<QcMonthlyReviewDTO>(this.qcMonthlyReviewEndpoint, qcMonthlyReview)
      .pipe(catchError(this.errorHandler));
  }

  updateMonthlyReview(qcMonthlyReview: QcMonthlyReviewDTO): Observable<SaveMonthlyReviewResponse> {
    return this.httpClient
      .put<QcMonthlyReviewDTO>(`${this.qcMonthlyReviewEndpoint}/${qcMonthlyReview.id}`, qcMonthlyReview)
      .pipe(catchError(this.errorHandler));
  }

  getMonthlyReviews(filter: any): Observable<HttpResponse<QcMonthlyReviewDTO[]>> {
    return this.httpClient
      .get<QcMonthlyReviewDTO[]>(this.qcMonthlyReviewEndpoint, {
        params: { ...filter },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  findQcMonthlyReviewById(qcMonthlyReviewId: number): Observable<HttpResponse<QcMonthlyReviewDTO>> {
    return this.httpClient
      .get<QcMonthlyReviewDTO>(`${this.qcMonthlyReviewEndpoint}/${qcMonthlyReviewId}`)
      .pipe(catchError(this.errorHandler));
  }

  deleteMonthlyReview(qcMonthlyReviewId: number): Observable<HttpResponse<void>> {
    return this.httpClient
      .delete(`${this.qcMonthlyReviewEndpoint}/${qcMonthlyReviewId}`)
      .pipe(catchError(this.errorHandler));
  }

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
