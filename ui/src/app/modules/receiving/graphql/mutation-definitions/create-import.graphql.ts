import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';

export interface CreateImportRequestDTO {
    temperatureCategory: string
    transitStartDateTime?: string
    transitStartTimeZone?: string
    transitEndDateTime?: string
    transitEndTimeZone?: string
    temperature?: number
    thermometerCode?: string
    locationCode: string
    comments?: string
    employeeId: string
}

export interface ImportDTO {
    id: number,
    temperatureCategory: string,
    transitStartDateTime: string,
    transitStartTimeZone: string,
    transitEndDateTime: string,
    transitEndTimeZone: string,
    totalTransitTime: string,
    transitTimeResult: string,
    temperature: string,
    thermometerCode: string,
    temperatureResult: string,
    locationCode: string,
    comments: string,
    status: string,
    employeeId: string,
    createDate: string,
    modificationDate: string,
    isQuarantined: boolean
}

export const CREATE_IMPORT = gql<
    {
        createImport: UseCaseResponseDTO<ImportDTO>
    },
    CreateImportRequestDTO
>`
    mutation CreateImport (
        $temperatureCategory: String!
        $transitStartDateTime: DateTime
        $transitStartTimeZone: String
        $transitEndDateTime: DateTime
        $transitEndTimeZone: String
        $temperature: Float
        $thermometerCode: String
        $locationCode: String!
        $comments: String
        $employeeId: String!
    ) {
        createImport(
            createImportRequest: {
                temperatureCategory: $temperatureCategory,
                transitStartDateTime: $transitStartDateTime,
                transitStartTimeZone: $transitStartTimeZone,
                transitEndDateTime: $transitEndDateTime,
                transitEndTimeZone: $transitEndTimeZone,
                temperature: $temperature,
                thermometerCode: $thermometerCode,
                locationCode: $locationCode,
                comments: $comments,
                employeeId: $employeeId,
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
