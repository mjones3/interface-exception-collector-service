import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { TranslateLoader } from '@ngx-translate/core';
import { Observable, throwError } from 'rxjs';
import { catchError, retry } from 'rxjs/operators';
import { EnvironmentConfigService } from '../../shared/services/environment-config.service';
import { HeaderValue } from '../../shared/types/header-value.enum';
import { getLoaderHeaders } from '../../shared/utils/utils';

@Injectable({
  providedIn: 'root',
})
export class TranslationLoaderService implements TranslateLoader {
  resourceUrl: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {}

  /*
   * Function used by loader to bring the default language.
   */
  getTranslation(language: string): Observable<any> {
    return this.getCustomTranslation(language, 'common');
  }

  /*
   * Method used to change the language based on a module
   */
  getCustomTranslation(language: string, module: string): Observable<any> {
    this.resourceUrl = this.config.env.serverApiURL + '/v1/translations';
    return this.httpClient
      .get(this.resourceUrl + '/' + language + '/module/' + module, {
        headers: getLoaderHeaders(null, HeaderValue.True),
      })
      .pipe(retry(1), catchError(this.errorHandler));
  }

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
