import { HttpClient, HttpErrorResponse, HttpHeaders, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { PgdTestTypesDto } from '../models/pgd-test-types.dto';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class PgdService {
  readonly pgdUri = 'v1/pgd';
  readonly pgdTestTypeUri = this.pgdUri + '/test-types';
  readonly pgdReagentControlUri = this.pgdUri + '/reagent-controls';
  readonly pgdReagentEntryUri = this.pgdUri + '/reagent-entries';
  readonly pgdControlEntryUri = this.pgdUri + '/control-entries';
  readonly pgdEntryUri = this.pgdUri + '/entries';

  readonly httpOptions: Object = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' }),
    observe: 'response',
  };

  constructor(private http: HttpClient, private config: EnvironmentConfigService) {}

  public findAllTestTypes(): Observable<HttpResponse<PgdTestTypesDto[]>> {
    return this.http
      .get<PgdTestTypesDto[]>(`${this.config.env.serverApiURL}/${this.pgdTestTypeUri}`, this.httpOptions)
      .pipe(catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
