import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface DeviceDTO {
    bloodCenterId: string;
    deviceType: string;
    deviceCategory: string;
    serialNumber: string;
    locationCode: string;
    name: string;
}

export interface ValidateDeviceRequestDTO {
    bloodCenterId: string;
    locationCode: string;
}

export const VALIDATE_DEVICE = gql<
    { validateDevice: UseCaseResponseDTO<DeviceDTO> },
    ValidateDeviceRequestDTO
>`
    query ValidateDevice(
        $bloodCenterId: String!
        $locationCode: String!
    ) {
        validateDevice(
            validateDeviceRequest: {
                bloodCenterId: $bloodCenterId,
                locationCode: $locationCode
            }
        ) {
            _links
            data
            notifications {
                message
                type
                code
                action
                reason
                details
                name
            }
        }
    }
`;
