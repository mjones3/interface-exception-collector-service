import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {
  ReleaseDto,
  ResearchAssignedInventoryDTO,
  ResearchDto,
  ResearchFlaggedInventoryDTO,
  ResearchInventoryDto,
} from '../models';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<ResearchDto>;
type EntityArrayResponseType = HttpResponse<ResearchDto[]>;
type ResearchFlaggedInventoriesResponse = HttpResponse<ResearchFlaggedInventoryDTO[]>;

@Injectable({
  providedIn: 'root',
})
export class ResearchService {
  researchEndpoint: string;
  assignedInventoriesEndpoint: string;
  researchFlaggedInventoriesEndpoint: string;

  constructor(private httpClient: HttpClient, private config: EnvironmentConfigService) {
    this.researchEndpoint = config.env.serverApiURL + '/v1/researches';
    this.assignedInventoriesEndpoint = config.env.serverApiURL + '/v1/research-assigned-inventories';
    this.researchFlaggedInventoriesEndpoint = config.env.serverApiURL + '/v1/research-flagged-inventories';
  }

  public getResearches(
    pageable,
    sortInfo?,
    filter?: {
      statusDescriptionKeys?: string;
      locationIds?: string;
      startDate?: string;
      endDate?: string;
    }
  ): Observable<EntityArrayResponseType> {
    const params = {
      ...pageable,
      ...sortInfo,
    };

    if (filter) {
      if (filter.statusDescriptionKeys) {
        params['status.in'] = filter.statusDescriptionKeys;
      }

      if (filter.locationIds) {
        params['locations'] = filter.locationIds;
      }

      if (filter.startDate) {
        params['startDate'] = filter.startDate;
      }

      if (filter.endDate) {
        params['endDate'] = filter.endDate;
      }
    }

    return this.httpClient
      .get<ReleaseDto[]>(this.researchEndpoint, {
        params,
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getResearch(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .get<ResearchDto>(this.researchEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getNextPriority(): Observable<HttpResponse<number>> {
    return this.httpClient
      .get<number>(this.researchEndpoint + '/next-priority', { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public updatePriority(id: number, priority: number): Observable<EntityResponseType> {
    return this.httpClient
      .put<ResearchDto>(
        this.researchEndpoint + '/' + id + '/priorities',
        { priority },
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public verifyProjectName(name: string): Observable<HttpResponse<{ exists: boolean }>> {
    return this.httpClient
      .get<{ exists: boolean }>(this.researchEndpoint + '/project-name-exists', {
        params: { name },
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public addResearchProject(dto): Observable<EntityResponseType> {
    return this.httpClient
      .post<ResearchDto>(this.researchEndpoint, dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public updateStatusToComplete(id: number): Observable<EntityResponseType> {
    return this.httpClient.put<ResearchDto>(
      this.researchEndpoint + '/update-status-to-complete/' + id,
      {},
      {
        observe: 'response',
      }
    );
  }

  //--------- ASSIGNED INVENTORIES ---------

  //#region
  public getAssignedInventoriesByCriteria(criteria): Observable<HttpResponse<ResearchAssignedInventoryDTO[]>> {
    return this.httpClient
      .get<ResearchAssignedInventoryDTO[]>(this.assignedInventoriesEndpoint, { params: criteria, observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public getResearchFlaggedInventoriesByInventoryId(id: number): Observable<ResearchFlaggedInventoriesResponse> {
    return this.httpClient
      .get<ResearchFlaggedInventoryDTO[]>(this.researchFlaggedInventoriesEndpoint + '?inventoryId=' + id, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public getResearchFlaggedInventoriesByInventoryIds(
    inventoryIds: number[]
  ): Observable<ResearchFlaggedInventoriesResponse> {
    const params = inventoryIds.reduce((sum, current) => `${sum},${current}`, '');
    return this.httpClient
      .get<ResearchFlaggedInventoryDTO[]>(
        `${this.researchFlaggedInventoriesEndpoint}?inventoryId.in=${params}&order=createDate.desc&size=1`,
        {
          observe: 'response',
        }
      )
      .pipe(catchError(this.errorHandler));
  }

  public assignInventories(
    id: number,
    dto: { inventoryId: number; productKey: string }[]
  ): Observable<HttpResponse<ResearchAssignedInventoryDTO[]>> {
    return this.httpClient
      .put<ResearchAssignedInventoryDTO[]>(this.researchEndpoint + '/' + id + '/assigned-inventories', dto, {
        observe: 'response',
      })
      .pipe(catchError(this.errorHandler));
  }

  public removeAllAssignedInventories(id: number): Observable<EntityResponseType> {
    return this.httpClient
      .delete<EntityResponseType>(this.researchEndpoint + '/' + id + '/assigned-inventories', { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }

  public removeAssignedInventory(id: number): Observable<HttpResponse<ResearchInventoryDto>> {
    return this.httpClient
      .delete<ResearchInventoryDto>(this.assignedInventoriesEndpoint + '/' + id, { observe: 'response' })
      .pipe(catchError(this.errorHandler));
  }
  //#endregion

  // Error handlers
  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }
}
