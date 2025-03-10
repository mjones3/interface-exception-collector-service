import { gql } from 'apollo-angular';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { ExternalTransferItemDTO } from '../models/external-transfer.dto';

const EXTERNAL_TRANSFER_ITEM = gql<
    { addExternalTransferProduct: RuleResponseDTO },
    ExternalTransferItemDTO
>`
    mutation addExternalTransferProduct(
        $externalTransferId: Int!
        $unitNumber: String!
        $employeeId: String!
        $productCode: String!
    ) {
        addExternalTransferProduct(
            addProductTransferRequestDTO: {
                externalTransferId: $externalTransferId
                unitNumber: $unitNumber
                employeeId: $employeeId
                productCode: $productCode
            }
        ) {
            ruleCode
            notifications {
                statusCode
                notificationType
                name
                action
                reason
                message
                details
            }
            _links
            results
        }
    }
`;

const COMPLETE_EXTERNAL_TRANSFER = gql<
    { completeExternalTransfer: RuleResponseDTO },
    {
        externalTransferId: number;
        employeeId: string;
        hospitalTransferId: string;
    }
>`
    mutation completeExternalTransfer(
        $externalTransferId: Int!
        $hospitalTransferId: String!
        $employeeId: String!
    ) {
        completeExternalTransfer(
            completeExternalTransferRequestDTO: {
                externalTransferId: $externalTransferId
                hospitalTransferId: $hospitalTransferId
                employeeId: $employeeId
            }
        ) {
            ruleCode
            notifications {
                statusCode
                notificationType
                message
            }
            _links
            results
        }
    }
`;

export { COMPLETE_EXTERNAL_TRANSFER, EXTERNAL_TRANSFER_ITEM };
