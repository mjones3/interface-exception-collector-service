import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { RareDonorDto, RareDonorWriteDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class RareDonorService {
  rareDonorEndpoint: string;

  constructor(private http: HttpClient, private config: EnvironmentConfigService) {
    this.rareDonorEndpoint = `${config.env.serverApiURL}/v1/rare-donors`;
  }

  getRareDonorPropertiesByDonorId = (donorId: number) => this.getRareDonorProperties({ donorId: donorId.toString() });

  getRareDonorProperties(criteria: { [key: string]: string }): Observable<HttpResponse<RareDonorDto[]>> {
    return this.http.get<RareDonorDto[]>(this.rareDonorEndpoint, {
      params: criteria,
      observe: 'response',
    });
  }

  deleteRareDonorProperties(donorId: number): Observable<HttpResponse<RareDonorDto[]>> {
    return this.http
      .delete(`${this.rareDonorEndpoint}/${donorId}`, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  updateRareDonorProperties(rareDonorWriteDto: RareDonorWriteDto): Observable<HttpResponse<RareDonorDto[]>> {
    return this.http
      .put<RareDonorDto[]>(`${this.rareDonorEndpoint}/${rareDonorWriteDto.donorId}`, rareDonorWriteDto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
