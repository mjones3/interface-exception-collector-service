import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { DonationTypeDto, VolumeTypeDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<VolumeTypeDto[]>;

//Common rest
@Injectable({
  providedIn: 'root',
})
export class VolumeTypeService {
  volumeTypeEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.volumeTypeEndpoint = config.env.serverApiURL + '/v1/volume-types?size=1000';
  }

  public getVolumeTypes(): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<DonationTypeDto[]>(this.volumeTypeEndpoint, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
