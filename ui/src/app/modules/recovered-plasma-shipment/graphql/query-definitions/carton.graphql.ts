import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { CartonDTO } from '../../models/recovered-plasma.dto';

export const FIND_CARTON_BY_ID = gql<
    {
        findCartonById: UseCaseResponseDTO<CartonDTO>;
    },
    {
        cartonId: number;
    }
>`
    query FindCartonById($cartonId: ID!) {
        findCartonById(cartonId: $cartonId) {
            _links
            data
            notifications {
                message
                type
                code
            }
        }
    }
`;
