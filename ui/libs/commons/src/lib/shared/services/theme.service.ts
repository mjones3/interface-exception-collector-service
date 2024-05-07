import { HttpClient, HttpParams, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TreoNavigationItem } from '@treo';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';
import { EnvironmentConfigService } from './environment-config.service';

type EntityArrayResponseType = HttpResponse<TreoNavigationItem[]>;

@Injectable({
  providedIn: 'root',
})
export class ThemeService {
  private resourceUrl: string;

  constructor(private http: HttpClient, private config: EnvironmentConfigService) {
    this.resourceUrl = config.env.serverApiURL + '/v1/themes';
  }

  getThemes(): Observable<EntityArrayResponseType> {
    const options = new HttpParams().set('page', '0').set('size', '100');
    return this.http
      .get<TreoNavigationItem[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(catchError(this.handleError));
  }

  private handleError(err) {
    /**
     * Handle logic here
     **/
    return throwError(err.message);
  }
}
