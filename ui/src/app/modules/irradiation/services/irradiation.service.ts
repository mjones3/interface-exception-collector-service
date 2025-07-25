import { Injectable } from '@angular/core';
import {DynamicGraphqlPathService} from "../../../core/services/dynamic-graphql-path.service";
import {Observable} from "rxjs";
import {ApolloQueryResult} from "@apollo/client";
import {MutationResult} from "apollo-angular";
import {
    CheckDigitResponseDTO,
    IrradiationProductDTO, ReadConfigurationDTO,
    StartIrradiationSubmitBatchRequestDTO, StartIrradiationSubmitBatchResponseDTO
} from "../models/model";
import {
    CHECK_DIGIT,
    GET_CONFIGURATIONS,
    GET_IRRADIATION_DEVICE_BY_ID,
    VALIDATE_LOT_NUMBER,
    VALIDATE_UNIT,
    VALIDATE_DEVICE_ON_CLOSE_BATCH
} from "../graphql/query.graphql";
import {START_IRRADIATION_SUBMIT_BATCH} from "../graphql/mutation.graphql";

@Injectable({
  providedIn: 'root'
})
export class IrradiationService {

    readonly servicePath = '/irradiation/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public readConfiguration(
        configurationKeys: string[]
    ): Observable<
        ApolloQueryResult<{ readConfiguration: ReadConfigurationDTO[] }>
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

    public validateUnitNumber(
        unitNumber: string, location: string
    ): Observable<
        ApolloQueryResult<{ validateUnit: IrradiationProductDTO[] }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_UNIT,
            { unitNumber, location }
        );
    }

    public startIrradiationSubmitBatch(
        startIrradiationSubmitBatchRequestDTO: StartIrradiationSubmitBatchRequestDTO
    ): Observable<MutationResult<{ response: StartIrradiationSubmitBatchResponseDTO }>> {
        return this.dynamicGraphqlPathService.executeMutation(
            this.servicePath,
            START_IRRADIATION_SUBMIT_BATCH,
            { input: startIrradiationSubmitBatchRequestDTO }
        );
    }

    public validateCheckDigit(
        unitNumber: string, checkDigit: string
    ): Observable<
        ApolloQueryResult<{ checkDigit: CheckDigitResponseDTO }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            CHECK_DIGIT,
            { unitNumber, checkDigit }
        );
    }

    public validateLotNumber(
        lotNumber: string, type: string
    ): Observable<
        ApolloQueryResult<{ validateLotNumber: boolean }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_LOT_NUMBER,
            { lotNumber, type }
        );
    }

    public validateDeviceOnCloseBatch(
        deviceId: string, location: string
    ): Observable<
        ApolloQueryResult<{ validateDeviceOnCloseBatch: IrradiationProductDTO[] }>
    > {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_DEVICE_ON_CLOSE_BATCH,
            { deviceId, location }
        );
    }

}
