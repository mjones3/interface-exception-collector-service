import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface ValidationResultDTO {
    valid: boolean;
    message: string;
    result: string;
}

export interface ValidateTemperatureRequestDTO {
    temperature: number;
    temperatureCategory: string;
}

export const VALIDATE_TEMPERATURE = gql<
    { validateTemperature: UseCaseResponseDTO<ValidationResultDTO> },
    ValidateTemperatureRequestDTO
>`
    query ValidateTemperature(
        $temperature: Float!
        $temperatureCategory: String!
    ) {
        validateTemperature(
            validateTemperatureRequest: {
                temperature: $temperature,
                temperatureCategory: $temperatureCategory
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
