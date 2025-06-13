import { Injectable } from '@angular/core';
import { DynamicGraphqlPathService } from '../../../core/services/dynamic-graphql-path.service';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { UseCaseResponseDTO } from '../../../shared/models/use-case-response.dto';
import {
    ENTER_SHIPPING_INFORMATION,
    EnterShippingInformationRequestDTO,
    ShippingInformationDTO
} from '../graphql/query-definitions/imports-enter-shipping-information.graphql';
import { LookUpDto } from '@shared';
import { FIND_ALL_LOOKUPS_BY_TYPE } from '../../recovered-plasma-shipment/graphql/query-definitions/lookup.graphql';
import {
    DeviceDTO,
    VALIDATE_DEVICE,
    ValidateDeviceRequestDTO
} from '../graphql/query-definitions/imports-validate-device.graphql';
import {
    VALIDATE_TEMPERATURE,
    ValidateTemperatureRequestDTO
} from '../graphql/query-definitions/imports-validate-temperature.graphql';
import { ValidationResultDTO } from '../models/validation-result-dto.model';
import {
    VALIDATE_TRANSIT_TIME,
    ValidateTransitTimeRequestDTO
} from '../graphql/query-definitions/imports-validate-transit-time.graphql';

@Injectable({
    providedIn: 'root',
})
export class ReceivingService {

    private readonly servicePath = '/receiving/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {
    }

    public findAllLookupsByType(type: string): Observable<ApolloQueryResult<{ findAllLookupsByType: LookUpDto[] }>> {
        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            FIND_ALL_LOOKUPS_BY_TYPE,
            { type }
        );
    }

    public queryEnterShippingInformation(enterShippingInformationRequestDTO: EnterShippingInformationRequestDTO)
        : Observable<ApolloQueryResult<{ enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            ENTER_SHIPPING_INFORMATION,
            enterShippingInformationRequestDTO
        );
    }

    public validateDevice(validateDeviceRequestDTO: ValidateDeviceRequestDTO)
        : Observable<ApolloQueryResult<{ validateDevice: UseCaseResponseDTO<DeviceDTO> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_DEVICE,
            validateDeviceRequestDTO
        );
    }

    public validateTemperature(validateTemperatureRequestDTO: ValidateTemperatureRequestDTO)
        : Observable<ApolloQueryResult<{ validateTemperature: UseCaseResponseDTO<ValidationResultDTO> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_TEMPERATURE,
            validateTemperatureRequestDTO
        );
    }

    public validateTransitTime(validateTransitTimeRequestDTO: ValidateTransitTimeRequestDTO)
        : Observable<ApolloQueryResult<{ validateTransitTime: UseCaseResponseDTO<ValidationResultDTO> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_TRANSIT_TIME,
            validateTransitTimeRequestDTO
        );
    }

}
