import { gql } from 'apollo-angular';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { CreateExternalTransferRequestDTO } from '../models/external-transfer.dto';

const VERIFY_TRANSFER_INFO = gql<
    { createExternalTransfer: RuleResponseDTO },
    CreateExternalTransferRequestDTO
>`
    mutation createExternalTransfer(
        $customerCode: String!
        $transferDate: Date!
        $hospitalTransferId: String!
        $createEmployeeId: String!
    ) {
        createExternalTransfer(
            createExternalTransferRequest: {
                customerCode: $customerCode
                transferDate: $transferDate
                hospitalTransferId: $hospitalTransferId
                createEmployeeId: $createEmployeeId
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

export { VERIFY_TRANSFER_INFO };
