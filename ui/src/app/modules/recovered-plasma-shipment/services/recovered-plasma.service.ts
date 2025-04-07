import { Injectable } from '@angular/core';
import { ApolloQueryResult } from '@apollo/client';
import { Observable, Observer } from 'rxjs';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import {
    FIND_ALL_CUSTOMERS,
    RecoveredPlasmaCustomerDTO,
} from '../graphql/query-definitions/customer.graphql';
import {
    FIND_ALL_LOCATIONS,
    RecoveredPlasmaLocationDTO,
} from '../graphql/query-definitions/location.graphql';
import { FacilityService } from '@shared';
import { CookieService } from 'ngx-cookie-service';
import { Cookie } from 'app/shared/types/cookie.enum';


@Injectable({
    providedIn: 'root',
})
export class RecoveredPlasmaService {
    readonly servicePath = '/recoveredplasmashipping/graphql';

    constructor(
        private dynamicGraphqlPathService: DynamicGraphqlPathService
    ) {}

    public findAllLocations(): Observable<
        ApolloQueryResult<{
            findAllLocations: RecoveredPlasmaLocationDTO[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_LOCATIONS
        );
    }

    public findAllCustomers(): Observable<
        ApolloQueryResult<{
            findAllCustomers: RecoveredPlasmaCustomerDTO[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_CUSTOMERS
        );
    }

    public checkRecoveredPlasmaFacility(facilityCode: string) {
        let matchFacility;
        return new Observable((observer: Observer<boolean>) => {
            this.findAllLocations().subscribe((res) => {
                matchFacility = res.data.findAllLocations.find(
                    (location) => location.code === facilityCode
                );
                observer.next(!!matchFacility);
                observer.complete();
            });
        });
    }
}
