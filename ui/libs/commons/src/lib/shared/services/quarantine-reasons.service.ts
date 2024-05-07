import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class QuarantineReasonsService {
  private quarantineReasonUrl = this.envConfig.env.serverApiURL + '/v1/quarantines-reasons';
  readonly loaderSelector = 'div.flex.flex-1.py-4.relative';

  constructor(private httpClient: HttpClient, private envConfig: EnvironmentConfigService) {}

  /*
   * Getting reasons by type
   */
  getReasonsByTypes(types: string): Observable<any> {
    const options: any = { observe: 'response' };
    return this.httpClient.get(this.quarantineReasonUrl + '?quarantineType.in=' + types, options);
  }
}
