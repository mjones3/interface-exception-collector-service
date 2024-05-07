import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ReagentDTO } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root',
})
export class ReagentsService {
  donationEndpoint: string;
  constructor(private http: HttpClient, private config: EnvironmentConfigService) {
    this.donationEndpoint = `${config.env.serverApiURL}/v1/reagents`;
  }
  getReagentsByCriteria(criteria: { [key: string]: string }): Observable<HttpResponse<ReagentDTO[]>> {
    return this.http.get<ReagentDTO[]>(this.donationEndpoint, {
      params: criteria,
      observe: 'response',
    });
  }
}
