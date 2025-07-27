import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';
import { TransferInformationDTO } from '../../models/internal-transfer-order.dto';

export interface ValidateTransferOrderNumberDTO {
    orderNumber: number;
    employeeId: string;
    locationCode: string;
}

export const VALIDATE_TRANSFER_ORDER_NUMBER = gql<
    { validateTransferOrderNumber: UseCaseResponseDTO<TransferInformationDTO> },
    ValidateTransferOrderNumberDTO
>`
    query validateTransferOrderNumber(
        $orderNumber: ID!
        $employeeId: String!
        $locationCode: String!
    ) {
        validateTransferOrderNumber(
            validateTransferOrderNumberRequest: {
                orderNumber: $orderNumber,
                employeeId: $employeeId
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
