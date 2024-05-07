import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { DeferralDeactivateReasonDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class DeferralReasonsService {
  deferralReasonEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.deferralReasonEndpoint = `${config.env.serverApiURL}/v1/deferral-reasons`;
  }

  getDeferralReasons(): Observable<DeferralDeactivateReasonDto[]> {
    return this.httpClient.get<DeferralDeactivateReasonDto[]>(this.deferralReasonEndpoint);
  }
}
