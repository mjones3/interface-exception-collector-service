import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';

export interface GenerateCartonLabelRequestDTO {
    cartonId: number;
    employeeId: string;
}

export interface LabelDTO {
    labelContent: string;
}

export const GENERATE_CARTON_LABEL = gql<
    { generateCartonLabel: UseCaseResponseDTO<LabelDTO> },
    GenerateCartonLabelRequestDTO
>`
    query GenerateCartonLabel(
        $cartonId: Int!,
        $employeeId: String!,
    ) {
        generateCartonLabel(generateCartonLabelRequest: {
            cartonId: $cartonId,
            employeeId: $employeeId
        }) {
            _links
            data
            notifications {
                message
                type
                code
                reason
                action
                details
            }
        }
    }
`;
