import { gql } from 'apollo-angular';
import { ValidationResultDTO } from '../../models/validation-result-dto.model';
import { UseCaseResponseDTO } from 'app/shared/models';

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
