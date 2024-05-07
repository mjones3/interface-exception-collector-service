import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ReagentsAndControlsEntryDto } from '../models/reagents-and-controls-entry.dto';
import { ReagentsAndControlsTypeDto } from '../models/reagents-and-controls-type.dto';
import { EnvironmentConfigService } from './environment-config.service';

type ReagentsAndControlsTypeResponse = HttpResponse<ReagentsAndControlsTypeDto>;
type EntityArrayResponseType = HttpResponse<ReagentsAndControlsTypeDto[]>;
type ReagentsAndControlsEntryResponse = HttpResponse<ReagentsAndControlsEntryDto>;

@Injectable({
  providedIn: 'root',
})
export class ReagentsAndControlsService {
  dataFound: boolean;
  reagentsAndControlsTypeEndpoint: string;
  reagentsAndControlsEntryEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.reagentsAndControlsTypeEndpoint = config.env.serverApiURL + '/v1/pqc/reagent-controls/types';
    this.reagentsAndControlsEntryEndpoint = config.env.serverApiURL + '/v1/pqc/entries';
  }

  // Get ReagentsAndControls Type
  public getReagentsAndControlsType(dto: ReagentsAndControlsTypeDto): Observable<ReagentsAndControlsTypeResponse> {
    return this.httpClient
      .get<ReagentsAndControlsTypeResponse>(this.reagentsAndControlsTypeEndpoint + dto.id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //Get All ReagentsAndControls Types
  public getAllReagentsAndControlsTypes(): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReagentsAndControlsTypeDto[]>(this.reagentsAndControlsTypeEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getAllReagentsAndControlsTypesByLocation(locationId: number): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<ReagentsAndControlsTypeDto[]>(this.reagentsAndControlsTypeEndpoint + '?locations=' + locationId, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  //PQC Entry

  public createReagentsAndControlsEntry(
    dto: ReagentsAndControlsEntryDto
  ): Observable<ReagentsAndControlsEntryResponse> {
    return this.httpClient
      .post<ReagentsAndControlsEntryDto>(this.reagentsAndControlsEntryEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updatedReagentsAndControlsEntry(
    dto: ReagentsAndControlsEntryDto
  ): Observable<ReagentsAndControlsEntryResponse> {
    return this.httpClient
      .put<ReagentsAndControlsEntryDto>(this.reagentsAndControlsEntryEndpoint + '/' + dto.id, dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getReagentsAndControlsEntry(id: number): Observable<ReagentsAndControlsEntryResponse> {
    return this.httpClient
      .get<ReagentsAndControlsEntryDto>(this.reagentsAndControlsEntryEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public reviewReagentsAndControlsEntry(entryId: number): Observable<ReagentsAndControlsEntryResponse> {
    return this.httpClient
      .patch<ReagentsAndControlsEntryDto>(this.reagentsAndControlsEntryEndpoint + '/' + entryId, null, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public deleteReagentsAndControlsEntry(entryId: number): Observable<ReagentsAndControlsEntryResponse> {
    return this.httpClient
      .delete<ReagentsAndControlsEntryDto>(this.reagentsAndControlsEntryEndpoint + '/' + entryId, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public doubleBlindReviewReagentsAndControlsEntry(
    dto: ReagentsAndControlsEntryDto
  ): Observable<ReagentsAndControlsEntryResponse> {
    return this.httpClient
      .patch<ReagentsAndControlsEntryDto>(
        this.reagentsAndControlsEntryEndpoint + '/' + dto.id + '/double-blind-review',
        dto,
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }
}
