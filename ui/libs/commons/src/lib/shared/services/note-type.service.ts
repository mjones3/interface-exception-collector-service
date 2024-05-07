import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { NoteTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<NoteTypeDto>;
type EntityArrayResponseType = HttpResponse<NoteTypeDto[]>;

@Injectable({
  providedIn: 'root',
})
export class NoteTypeService {
  donorNoteEndpoint: string;
  createDonorNoteEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.donorNoteEndpoint = config.env.serverApiURL + '/v1/note-types';
    this.createDonorNoteEndpoint = this.donorNoteEndpoint;
  }

  public getNoteTypes(): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<NoteTypeDto[]>(this.donorNoteEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getNoteType(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<NoteTypeDto>(`${this.donorNoteEndpoint}?${id}`, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
