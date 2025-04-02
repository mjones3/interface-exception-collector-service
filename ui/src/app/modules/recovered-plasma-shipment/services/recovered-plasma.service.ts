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
import { LookUpDto } from '@shared';
import { FIND_ALL_LOOKUPS_BY_TYPE } from '../graphql/query-definitions/lookup.graphql';
import { UseCaseResponseDTO } from '../../../shared/models/use-case-response.dto';
import { PageDTO } from '../../../shared/models/page.model';
import {
    RecoveredPlasmaShipmentQueryCommandRequestDTO,
    RecoveredPlasmaShipmentReportDTO,
    SEARCH_RP_SHIPMENT
} from '../graphql/query-definitions/shipment.graphql';

@Injectable({
    providedIn: 'root',
})
export class RecoveredPlasmaService {
    readonly servicePath = '/recoveredplasmashipping/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public searchRecoveredPlasmaShipments(
        recoveredPlasmaShipmentQueryCommandRequestDTO: RecoveredPlasmaShipmentQueryCommandRequestDTO
    ): Observable<
        ApolloQueryResult<{
            searchShipment: UseCaseResponseDTO<
                PageDTO<RecoveredPlasmaShipmentReportDTO>
            >;
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            SEARCH_RP_SHIPMENT,
            { recoveredPlasmaShipmentQueryCommandRequestDTO }
        );
    }

    public findAllLookupsByType(type: string): Observable<
        ApolloQueryResult<{
            findAllLookupsByType: LookUpDto[];
        }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_LOOKUPS_BY_TYPE,
            { type }
        );
    }

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
