import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { VolumeDto, VolumeLossDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<VolumeDto>;
type EntityArrayResponseType = HttpResponse<VolumeDto[]>;

@Injectable({
  providedIn: 'root',
})
export class VolumeService {
  volumeEndpoint: string;
  totalVolumeLossEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.volumeEndpoint = config.env.serverApiURL + '/v1/volumes';
    this.totalVolumeLossEndpoint = config.env.serverApiURL + '/v1/volume-groups/{groupId}/donors/{donorId}/totals';
  }

  public createVolume(dto: VolumeDto): Observable<EntityResponseType> {
    return this.httpClient
      .post<VolumeDto>(this.volumeEndpoint, dto, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getVolumeByDonationId(donationId: string): Observable<EntityArrayResponseType> {
    return this.httpClient
      .get<VolumeDto[]>(this.volumeEndpoint, { params: { donationId }, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getVolumeLoss(groupId: number, donorId: number): Observable<HttpResponse<VolumeLossDto>> {
    return this.httpClient
      .get<VolumeLossDto>(
        this.totalVolumeLossEndpoint.replace('{groupId}', `${groupId}`).replace('{donorId}', `${donorId}`),
        { observe: 'response' }
      )
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
