import { gql } from "apollo-angular";
import { UseCaseResponseDTO } from "app/shared/models/use-case-response.dto";

export interface CancelImportRequestDTO{
    importId: number,
    cancelEmployeeId: string
}

export const CONFIRM_CANCEL_IMPORT_PROCESS = gql<
    { cancelImport: UseCaseResponseDTO<void> },
    CancelImportRequestDTO
>`
    mutation cancelImport(
        $importId: ID!
        $cancelEmployeeId: String!
    ) {
        cancelImport(
            cancelImportRequest: {
                importId: $importId
                cancelEmployeeId: $cancelEmployeeId
            }
        ) {
            data
            notifications {
                code
                type
                message
            }
            _links
        }
    }
`;