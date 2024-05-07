import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DocumentDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type referenceTypeOptions = 'release' | 'bact' | 'ORDER_ITEM_MEDICAL_FORM' | 'EVENT_MANAGAMENT_FILE' | 'DONOR_HISTORY';

type EntityResponseType = HttpResponse<DocumentDto>;
type EntityArrayResponseType = HttpResponse<DocumentDto[]>;

@Injectable({
  providedIn: 'root',
})
export class FileService {
  downloadDocumentEndpoint: string;
  documentEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.documentEndpoint = config.env.serverApiURL + '/v1/documents';
    this.downloadDocumentEndpoint = config.env.serverApiURL + '/v1/documents/download';
  }

  public getDocuments(referenceId: string, referenceType: referenceTypeOptions): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DocumentDto[]>(this.documentEndpoint, {params: { referenceId, referenceType }, observe: 'response'})
      .pipe(catchError(this.errorHandler));
  }

  public getDocumentsByCriteria(criteria?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DocumentDto[]>(this.documentEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public uploadDocument(
    referenceId: number,
    referenceType: referenceTypeOptions,
    dto: FormData
  ): Observable<HttpResponse<DocumentDto[]>> {
    return this.httpClient
      .post<DocumentDto[]>(this.documentEndpoint + '/' + referenceId + '/' + referenceType, dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public downloadDocument(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<DocumentDto>(this.downloadDocumentEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public deleteDocument(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .delete<DocumentDto>(this.documentEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
