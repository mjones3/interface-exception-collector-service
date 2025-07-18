import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../models/use-case-response.dto';
import { ValidationResultDTO } from '../../models/validation-result-dto.model';

export interface ValidateTransitTimeRequestDTO {
    temperatureCategory: string;
    startDateTime: string;
    startTimeZone: string;
    endDateTime: string;
    endTimeZone: string;
}

export const VALIDATE_TRANSIT_TIME = gql<
    { validateTransitTime: UseCaseResponseDTO<ValidationResultDTO> },
    ValidateTransitTimeRequestDTO
>`
    query ValidateTransitTime(
        $temperatureCategory: String!
        $startDateTime: DateTime!
        $startTimeZone: String!
        $endDateTime: DateTime!
        $endTimeZone: String!
    ) {
        validateTransitTime(
            validateTransitTimeRequest: {
                temperatureCategory: $temperatureCategory,
                startDateTime: $startDateTime,
                startTimeZone: $startTimeZone,
                endDateTime: $endDateTime,
                endTimeZone: $endTimeZone
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