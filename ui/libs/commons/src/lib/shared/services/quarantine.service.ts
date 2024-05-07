import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { QuarantineDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class QuarantineService {
  private quarantineUrl = this.envConfig.env.serverApiURL + '/v1/quarantines';

  constructor(private _httpClient: HttpClient, private envConfig: EnvironmentConfigService) {}

  /**
   * Create Quarantine (POST)
   * @param quarantine Contains the QuarantineDto to be Saved
   */
  createQuarantine(quarantine: QuarantineDto): Observable<HttpResponse<QuarantineDto>> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    quarantine.id = null;
    return this._httpClient
      .post<QuarantineDto>(this.quarantineUrl, quarantine, {
        headers: headers,
        observe: 'response',
      })
      .pipe(catchError(this.handleError));
  }

  /**
   * Delete Quarantine (DELETE)
   * @param idQuarantine
   * @param releaseComments
   */
  deleteQuarantine(idQuarantine: string, releaseComments: string): Observable<any> {
    const httpOptions: any = {
      headers: {
        'Content-Type': 'text/plain',
      },
      observe: 'response',
      body: releaseComments,
    };
    const uri = this.quarantineUrl + '/' + idQuarantine;
    return this._httpClient.request('delete', uri, httpOptions).pipe(catchError(this.handleError));
  }
  /**
   * Get Quarantines (GET) Returns QuarantineDto Collection
   * @param donationId - Contains donationId from the Unit Number
   * @param pageable - Contains the pagination information
   */
  getAllQuarantines(donationId: number, includeInactive: boolean, pageable): Observable<HttpResponse<QuarantineDto[]>> {
    return this._httpClient.get<QuarantineDto[]>(
      `${this.quarantineUrl}/donation?donationId=${donationId}&includeInactive=${includeInactive}`,
      {
        params: pageable,
        observe: 'response',
      }
    );
  }

  /**
   * Get Quarantines (GET) Returns QuarantineDto Collection
   * @param filter - map filter
   * @param pageable - Contains the pagination information
   */
  getAllQuarantinesByCriteria(criteria, pageable): Observable<HttpResponse<QuarantineDto[]>> {
    return this._httpClient.get<QuarantineDto[]>(`${this.quarantineUrl}/donation`, {
      params: { ...criteria, ...pageable },
      observe: 'response',
    });
  }

  initializeQuarantine(): QuarantineDto {
    // Return an initialized object of type QuarantineDto
    return {
      id: null,
      inventoryId: null,
      employeeId: null,
      reasonId: null,
      reasonKey: null,
      comment: null,
      createDate: null,
      donationId: null,
    };
  }

  private handleError(err) {
    /**
     * Handle logic here
     **/
    return throwError(err.message);
  }
}
