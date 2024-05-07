import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PatientDto, PractitionerDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PatientService {
  patientEndpoint: string;
  practitionerEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.patientEndpoint = config.env.serverApiURL + '/v1/patients';
    this.practitionerEndpoint = config.env.serverApiURL + '/v1/practitioners/';
  }

  //#region PATIENT

  public getPatientByCriteria(criteria?: {}): Observable<PatientDto[]> {
    return this.httpClient
      .get<PatientDto[]>(this.patientEndpoint, { params: { ...criteria } })
      .pipe(catchError(this.errorHandler));
  }

  public createPatient(dto: PatientDto): Observable<PatientDto> {
    return this.httpClient.post<PatientDto>(this.patientEndpoint, dto).pipe(catchError(this.errorHandler));
  }

  public deletePatient(id: number): Observable<PatientDto> {
    return this.httpClient.delete<PatientDto>(`${this.patientEndpoint}/${id}`).pipe(catchError(this.errorHandler));
  }

  //#endregion

  //#region PRACTITIONER

  public getPractitionerById(id: number): Observable<HttpResponse<PractitionerDto[]>> {
    return this.httpClient
      .get<PractitionerDto[]>(this.practitionerEndpoint + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  //#endregion

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
