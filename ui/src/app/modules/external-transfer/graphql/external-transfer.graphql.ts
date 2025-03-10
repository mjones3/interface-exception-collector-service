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

export interface CancelExternalTransferRequest {
    externalTransferId: number;
    employeeId: string;
}

export interface ExternalTransferDTO {
    id: number;
}

export const CANCEL_EXTERNAL_TRANSFER_PROCESS = gql<
    { cancelExternalTransfer: RuleResponseDTO<{ results: never }> },
    CancelExternalTransferRequest
>`
    mutation cancelExternalTransfer(
        $externalTransferId: Int!
        $employeeId: String!
    ) {
        cancelExternalTransfer(
            cancelExternalTransferRequestDTO: {
                externalTransferId: $externalTransferId
                employeeId: $employeeId
            }
        ) {
            ruleCode
            _links
            results
            notifications {
                name
                statusCode
                notificationType
                code
                action
                reason
                message
            }
        }
    }
`;

export const CONFIRM_CANCEL_EXTERNAL_TRANSFER_PROCESS = gql<
    { confirmCancelExternalTransfer: RuleResponseDTO<{ results: never }> },
    CancelExternalTransferRequest
>`
    mutation confirmCancelExternalTransfer(
        $externalTransferId: Int!
        $employeeId: String!
    ) {
        confirmCancelExternalTransfer(
            cancelExternalTransferRequestDTO: {
                externalTransferId: $externalTransferId
                employeeId: $employeeId
            }
        ) {
            ruleCode
            _links
            results
            notifications {
                name
                statusCode
                notificationType
                code
                action
                reason
                message
            }
        }
    }
`;

export { VERIFY_TRANSFER_INFO };
