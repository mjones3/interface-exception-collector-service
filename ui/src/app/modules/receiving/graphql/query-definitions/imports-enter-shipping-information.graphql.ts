import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { LookUpDto } from '@shared';

enum TemperatureProductCategory {
    FROZEN,
    REFRIGERATED,
    ROOM_TEMPERATURE
}

export const temperatureProductCategoryCssMap: Record<keyof typeof TemperatureProductCategory, string> = {
    FROZEN: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-blue-100 text-blue-700',
    REFRIGERATED: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-purple-100 text-purple-700',
    ROOM_TEMPERATURE: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-red-100 text-red-700',
};

export enum VisualInspection {
    SATISFACTORY,
    UNSATISFACTORY
}

export enum Quarantined {
    TRUE,
    FALSE
}

export const visualInspectionCssMap: Record<keyof typeof VisualInspection, string> = {
    UNSATISFACTORY: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-red-100 text-red-700',
    SATISFACTORY: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-green-100 text-green-700',
};

export const quarantinedValueMap: Record<keyof typeof Quarantined, string> = {
    TRUE: 'YES',
    FALSE: '-',
};

export const quarantinedCssMap: Record<keyof typeof Quarantined, string> = {
    TRUE: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-[#FFEDD5] text-[#C2410C]',
    FALSE: '',
};

export const quarantinedValueMap: Record<keyof typeof Quarantined, string> = {
    TRUE: 'YES',
    FALSE: '-',
};

export const quarantinedCssMap: Record<keyof typeof Quarantined, string> = {
    TRUE: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-[#FFEDD5] text-[#C2410C]',
    FALSE: '',
};

export enum LicenseStatus {
    LICENSED,
    UNLICENSED
}

export const licenseStatusCssMap: Record<keyof typeof LicenseStatus, string> = {
    LICENSED: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-green-100 text-green-700',
    UNLICENSED: 'text-sm font-bold py-1.5 px-2 badge rounded-full bg-gray-100 text-gray-700',
};


export const TemperatureProductCategoryIconMap: Record<keyof typeof TemperatureProductCategory, string> = {
    FROZEN: 'biopro:temperature-freeze',
    REFRIGERATED: 'biopro:temperature-refrigerator',
    ROOM_TEMPERATURE: 'biopro:temperature-room-temperature',
}


export const TemperatureProductCategoryValueMap: Record<keyof typeof TemperatureProductCategory, string> = {
    FROZEN: 'FROZEN',
    REFRIGERATED: 'REFRIGERATED',
    ROOM_TEMPERATURE: 'ROOM TEMPERATURE',
}

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
