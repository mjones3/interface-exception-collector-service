import { HttpClient, HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { Injectable, Type } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { CookieService } from 'ngx-cookie-service';
import { BehaviorSubject, Observable, Observer, of, throwError } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Facility } from '../models';
import { Cookie } from '../types/cookie.enum';
import { EnvironmentConfigService } from './environment-config.service';
import { ALL_FACILITIES, FACILITY_BY_ID } from './mocks/facilities-mock';

type EntityResponseType = HttpResponse<Facility>;
type EntityArrayResponseType = HttpResponse<Facility[]>;

declare var dT_: any;

@Injectable({
  providedIn: 'root',
})
export class FacilityService {
  private facility: BehaviorSubject<Facility> = new BehaviorSubject<Facility>(null);
  facility$: Observable<Facility> = this.facility.asObservable();

  constructor(
    private httpClient: HttpClient,
    private config: EnvironmentConfigService,
    private cookieService: CookieService,
    private matDialog: MatDialog
  ) {
    if (typeof dT_ !== 'undefined' && dT_.initAngularNg) {
      dT_.initAngularNg(httpClient, Headers);
    }
  }

  async syncCookieAndService() {
    const all = this.cookieService.getAll();
    if (all[Cookie.XFacility] && !this.facility.getValue()) {
      try {
        const facility = await this.getFacilityById(+all[Cookie.XFacility]).toPromise();
        this.facility.next(facility.body);
      } catch (error) {
        this.cookieService.delete(Cookie.XFacility);
      }
    }
  }

  getFacilityId(): number {
    const currentFacility = this.facility.getValue();
    return currentFacility ? currentFacility.id : null;
  }

  getFacilityProperty(propertyName: string): string {
    const currentFacility = this.facility.getValue();
    return currentFacility && currentFacility.properties ? currentFacility.properties[propertyName] : null;
  }

  setFacility(facility: Facility): void {
    if (facility) {
      // Store the facility id in cookies
      this.cookieService.set(Cookie.XFacility, String(facility.id), 365, '/');
    } else {
      this.cookieService.delete(Cookie.XFacility);
    }
    this.facility.next(facility);
  }

  getAllFacilities(params?: { [key: string]: any }): Observable<EntityArrayResponseType> {
    return of(ALL_FACILITIES);
    /*
    return this.httpClient.get<Facility[]>(`${this.config.env.serverApiURL}/v1/locations?size=1000&sort=name,asc`, {
      params,
      observe: 'response',
    });
     */
  }

  getFacilityById(id: number): Observable<EntityResponseType> {
    return of(FACILITY_BY_ID(id));
    /*
    return this.httpClient.get<Facility>(`${this.config.env.serverApiURL}/v1/facilities/${id}`, {
      observe: 'response',
    });
     */
  }

  checkFacilityCookie(): boolean {
    return this.cookieService.check(Cookie.XFacility);
  }

  public errorHandler(error: HttpErrorResponse): Observable<any> {
    return throwError(error);
  }

  getFacilityDialog(component: Type<any>, locationTypes: number[], closable = false): Observable<any> {
    const title = document.querySelector('.module-title').innerHTML;
    return this.getAllFacilities({ 'locationTypeId.in': locationTypes.length ? locationTypes.join(',') : [] }).pipe(
      switchMap(options => {
        const defaults = {
          height: 'auto',
          disableClose: !closable,
          data: {
            options: options.body,
            optionsLabel: 'name',
            dialogTitle: title === 'Specialty Lab' ? 'specialty-lab-location.label' : 'select-facility.label',
            closable,
          },
        };
        return new Observable((observer: Observer<boolean>) => {
          // Open Facility Modal
          const dialogRef: MatDialogRef<any> = this.matDialog.open(component, defaults);
          // After close the modal set the facility
          dialogRef.afterClosed().subscribe(result => {
            if (result) {
              this.setFacility(result);
            }
            observer.next(!!result);
            observer.complete();
          });
        });
      })
    );
  }
}
