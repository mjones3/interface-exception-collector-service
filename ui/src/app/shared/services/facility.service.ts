import {
    HttpClient,
    HttpErrorResponse,
    HttpResponse,
} from '@angular/common/http';
import { Injectable, Type } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { CookieService } from 'ngx-cookie-service';
import { BehaviorSubject, Observable, Observer, throwError } from 'rxjs';
import { switchMap } from 'rxjs/operators';
import { Facility } from '../models';
import { Cookie } from '../types/cookie.enum';
import { EnvironmentConfigService } from './environment-config.service';

type EntityResponseType = HttpResponse<Facility>;
type EntityArrayResponseType = HttpResponse<Facility[]>;

declare const dT_: any;

@Injectable({
    providedIn: 'root',
})
export class FacilityService {
    private facility: BehaviorSubject<Facility> = new BehaviorSubject<Facility>(
        null
    );
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

    checkFacilityCookie(): boolean {
        return this.cookieService.check(Cookie.XFacility);
    }

    public errorHandler(error: HttpErrorResponse): Observable<any> {
        return throwError(error);
    }

    getAllFacilities(params?: object): Observable<EntityArrayResponseType> {
        // create new params adding the parameters size=1000&sort=name,asc
        params = { ...params, size: 1000, sort: 'name,asc' };
        return this.httpClient.get<Facility[]>(
            `${this.config.env.serverApiURL}/v1/locations`,
            {
                ...params,
                observe: 'response',
            }
        );
    }

    getFacilityById(id: number): Observable<EntityResponseType> {
        return this.httpClient.get<Facility>(
            `${this.config.env.serverApiURL}/v1/facilities/${id}`,
            {
                observe: 'response',
            }
        );
    }

    getFacilityDialog(component: Type<any>, closable = false): Observable<any> {
        const title = document.querySelector('.module-title').innerHTML;
        return this.getAllFacilities().pipe(
            switchMap((options) => {
                const defaults = {
                    height: 'auto',
                    disableClose: !closable,
                    data: {
                        options: options.body,
                        optionsLabel: 'name',
                        //TODO: Change to propert application name
                        dialogTitle:
                            title === 'Distribution'
                                ? 'Distribution Location'
                                : 'Select Facility',
                        closable,
                        iconName:'search'
                    },
                };
                return new Observable((observer: Observer<boolean>) => {
                    // Open Facility Modal
                    const dialogRef: MatDialogRef<any> = this.matDialog.open(
                        component,
                        defaults
                    );
                    // After close the modal set the facility
                    dialogRef.afterClosed().subscribe((result) => {
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

    getFacilityId(): number {
        const currentFacility = this.facility.getValue();
        return currentFacility ? currentFacility.id : null;
    }

    getFacilityProperty(propertyName: string): string {
        const currentFacility = this.facility.getValue();
        return currentFacility && currentFacility.properties
            ? currentFacility.properties[propertyName]
            : null;
    }

    setFacility(facility: Facility): void {
        if (facility) {
            // Store the facility id in cookies
            this.cookieService.set(
                Cookie.XFacility,
                String(facility.id),
                365,
                '/'
            );
        } else {
            this.cookieService.delete(Cookie.XFacility);
        }
        this.facility.next(facility);
    }

    async syncCookieAndService() {
        const all = this.cookieService.getAll();
        if (all[Cookie.XFacility] && !this.facility.getValue()) {
            try {
                const facility = await this.getFacilityById(
                    +all[Cookie.XFacility]
                ).toPromise();
                this.facility.next(facility.body);
            } catch {
                this.cookieService.delete(Cookie.XFacility);
            }
        }
    }
}
