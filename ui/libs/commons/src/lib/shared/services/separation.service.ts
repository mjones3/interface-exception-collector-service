import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SeparationDto } from '../models';
import { EnvironmentConfigService } from './environment-config.service';

@Injectable({
  providedIn: 'root'
})
export class SeparationService {

  constructor(private http: HttpClient, private envConfig: EnvironmentConfigService) {
  }

  getSeparation(inventoryId: string): Observable<HttpResponse<SeparationDto>> {
    return this.http.get<SeparationDto>(`${this.envConfig.env.serverApiURL}/v1/inventories/${inventoryId}/separations`, {observe: 'response'});
  }
}
