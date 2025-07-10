import { Injectable } from '@angular/core';
import {DynamicGraphqlPathService} from "../../../core/services/dynamic-graphql-path.service";
import {Observable} from "rxjs";
import {ApolloQueryResult} from "@apollo/client";
import {MutationResult} from "apollo-angular";
import {DeviceDTO, ReadConfigurationGraphQL, SubmitIrradiationBatchRequestDTO} from "../models/model";
import {GET_CONFIGURATIONS, GET_IRRADIATION_DEVICE_BY_ID} from "../graphql/query.graphql";
import {RuleResponseDTO} from "../../../shared/models/rule.model";
import {SUBMIT_IRRADIATION_BATCH} from "../graphql/mutation.graphql";

@Injectable({
  providedIn: 'root'
})
export class IrradiationService {

    readonly servicePath = '/irradiation/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public readConfiguration(
        configurationKeys: string[]
    ): Observable<
        ApolloQueryResult<{ readConfiguration: ReadConfigurationGraphQL }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_CONFIGURATIONS,
            { keys: configurationKeys }
        );
    }

    public loadDeviceById(
        deviceId: string, location: string
    ): Observable<
        ApolloQueryResult<{ validateDevice: boolean }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            GET_IRRADIATION_DEVICE_BY_ID,
            { deviceId, location }
        );
    }

    public submitCentrifugationBatch(
        dto: SubmitIrradiationBatchRequestDTO
    ): Observable<MutationResult<{ packItem: RuleResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            SUBMIT_IRRADIATION_BATCH,
            dto
        );
    }

}
