import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { LabelDto, LabelPrintDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

//Common rest
@Injectable({
  providedIn: 'root',
})
export class LabelingService {
  labelValidationEndpoint: string;
  labelPrintEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.labelValidationEndpoint = config.env.serverApiURL + '/v1/labels';
    // this is the url to get the agent to print.
    this.labelPrintEndpoint = 'http://localhost:8282/agent/print';
  }

  public createLabel(dto: LabelDto): Observable<HttpResponse<LabelDto>> {
    return this.httpClient
      .post<LabelDto>(this.labelValidationEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLabels(
    donationId: string,
    includeDeleted: boolean,
    includeVoided: boolean
  ): Observable<HttpResponse<LabelDto[]>> {
    let url = this.labelValidationEndpoint;
    if (includeDeleted) {
      url = `${this.labelValidationEndpoint}?donationId=${donationId}&includeDeleted=true&voidDate.specified=${includeVoided}`;
    } else {
      url = `${this.labelValidationEndpoint}?donationId=${donationId}&voidDate.specified=${includeVoided}`;
    }
    return this.httpClient
      .get<LabelDto[]>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getActiveAndValidatedLabel(inventoryId: number) {
    const url = `${this.labelValidationEndpoint}?inventoryId=${inventoryId}&deleteDate.specified=false&validationDate.specified=true&voidDate.specified=false`;
    return this.httpClient
      .get<LabelDto>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getActiveAndValidatedLabels(inventoryIds: number[]) {
    const url = `${this.labelValidationEndpoint}?inventoryId.in=${inventoryIds}&deleteDate.specified=false&validationDate.specified=true&voidDate.specified=false`;
    return this.httpClient
      .get<LabelDto[]>(url, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updateLabel(id: number, dto: LabelDto): Observable<HttpResponse<LabelDto>> {
    return this.httpClient
      .put<LabelDto>(this.labelValidationEndpoint + '/' + id, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public printLabel(dto: LabelPrintDto): Observable<any> {
    return this.httpClient
      .post<any>(this.labelPrintEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getLabelByCriteria(criteria?: { [key: string]: any }): Observable<HttpResponse<LabelDto[]>> {
    return this.httpClient
      .get<LabelDto[]>(this.labelValidationEndpoint, { params: { ...criteria }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public voidLabel(inventoryId: number): Observable<any> {
    return this.httpClient
      .post<any>(`${this.labelValidationEndpoint}/void`, inventoryId, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
