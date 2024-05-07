import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NoteDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<NoteDto>;
type EntityArrayResponseType = HttpResponse<NoteDto[]>;

@Injectable({
  providedIn: 'root',
})
export class NoteService {
  noteEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.noteEndpoint = config.env.serverApiURL + '/v1/notes';
  }

  public getPaginatedNotesByCriteria(params): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<NoteDto[]>(this.noteEndpoint, { params, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getPaginatedNotes(
    pageable,
    referenceId: number,
    typeKey: string[],
    sortInfo?
  ): Observable<EntityArrayResponseType> {
    const params = {
      ...pageable,
      referenceId,
      'typeKey.in': typeKey.join(','),
      ...sortInfo,
    };

    return this.httpClient
      .get<NoteDto[]>(this.noteEndpoint, { params: params, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public createNote(dto: NoteDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<NoteDto>(this.noteEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getNoteByReferenceIdAndType(referenceId: any, typeKey: string): Observable<HttpResponse<NoteDto[]>> {
    const params = {
      referenceId,
      typeKey,
    };

    return this.httpClient
      .get<NoteDto[]>(this.noteEndpoint, { observe: 'response', params: params })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
