import { Injectable } from '@angular/core';
import { DynamicGraphqlPathService } from '../../core/services/dynamic-graphql-path.service';
import { Observable } from 'rxjs';
import { ApolloQueryResult } from '@apollo/client';
import { UseCaseResponseDTO } from '../models/use-case-response.dto';
import { ValidationResultDTO } from '../models/validation-result-dto.model';
import { VALIDATE_TEMPERATURE, ValidateTemperatureRequestDTO } from '../graphql/temperature/imports-validate-temperature.graphql';
import { DeviceDTO, VALIDATE_DEVICE, ValidateDeviceRequestDTO } from '../graphql/device/imports-validate-device.graphql';

@Injectable({
    providedIn: 'root',
})
export class TemperatureDeviceService {
    private readonly servicePath = '/receiving/graphql';

    constructor(private dynamicGraphqlPathService: DynamicGraphqlPathService) {}

    public validateTemperature(validateTemperatureRequestDTO: ValidateTemperatureRequestDTO)
        : Observable<ApolloQueryResult<{ validateTemperature: UseCaseResponseDTO<ValidationResultDTO> }>> {

        return this.dynamicGraphqlPathService.executeQuery(
            this.servicePath,
            VALIDATE_TEMPERATURE,
            validateTemperatureRequestDTO
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
}