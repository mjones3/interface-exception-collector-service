import { HttpClient, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { Discard } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DiscardService {
  private discardUrl: string;

  constructor(private http: HttpClient, private config: EnvironmentConfigService) {
    this.discardUrl = config.env.serverApiURL + '/v1/discards';
  }

  createDiscard(discard: Discard): Observable<HttpResponse<Discard>> {
    const headers = new HttpHeaders({ 'Content-Type': 'application/json' });
    discard.id = null;
    return this.http
      .post<Discard>(this.discardUrl, discard, { headers: headers, observe: 'response' })
      .pipe(
        tap(data => console.log('createDiscard: ' + JSON.stringify(data))),
        catchError(this.handleError)
      );
  }

  public initializeDiscard(): Discard {
    // Return an initialized object
    return {
      id: 0,
      inventoryId: 0,
      reasonId: 0,
      employeeId: '',
      reasonKey: '',
      deleteDate: null,
      discardType: 'MANUAL',
      comments: '',
      createDate: new Date(),
    };
  }

  private handleError(err) {
    /**
     * Handle logic here
     **/
    return throwError(err.message);
  }

  discardCurrentInventory(reason: string, inventoryId: number, comments?: string): Observable<HttpResponse<Discard>> {
    const discard: Discard = this.initializeDiscard();
    discard.reasonKey = reason;
    discard.inventoryId = inventoryId;
    discard.comments = comments;
    //Discard the Item with reason
    return this.createDiscard(discard);
  }
}
