import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { LookUpDto } from '@shared';

export interface EnterShippingInformationRequestDTO {
    productCategory: string;
    employeeId: string;
    locationCode: string;
}

export interface ShippingInformationDTO {
    productCategory: string;
    temperatureUnit: string;
    displayTransitInformation: boolean;
    displayTemperature: boolean;
    transitTimeZoneList: LookUpDto[];
    visualInspectionList: LookUpDto[];
    defaultTimeZone: string;
}

export const ENTER_SHIPPING_INFORMATION = gql<
    { enterShippingInformation: UseCaseResponseDTO<ShippingInformationDTO> },
    EnterShippingInformationRequestDTO
>`
    query EnterShippingInformation(
        $productCategory: String!
        $employeeId: String!
        $locationCode: String!
    ) {
        enterShippingInformation(enterShippingInformationRequest: {
            productCategory: $productCategory,
            employeeId: $employeeId,
            locationCode: $locationCode
        }) {
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
