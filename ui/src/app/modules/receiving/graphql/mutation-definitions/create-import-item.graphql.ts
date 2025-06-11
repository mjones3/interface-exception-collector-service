import { gql } from "apollo-angular";
import { UseCaseResponseDTO } from "app/shared/models/use-case-response.dto";
import { AddImportItemRequestDTO, CreateImportResponsetDTO } from "../../models/product-information.dto";


export const CREATE_IMPORT_ITEM = gql<
    { createImportItem: UseCaseResponseDTO<CreateImportResponsetDTO> },
    AddImportItemRequestDTO
>`
    mutation createImportItem(
        $importId:ID!
        $unitNumber:String!
        $productCode:String!
        $aboRh:String!
        $expirationDate:DateTime!
        $visualInspection:String!
        $licenseStatus:String!
        $employeeId:String!
    ) {
        createImportItem(
            createImportItemRequest: {
                importId: $importId
                unitNumber: $unitNumber
                productCode: $productCode
                aboRh: $aboRh
                expirationDate: $expirationDate
                visualInspection: $visualInspection
                licenseStatus: $licenseStatus
                employeeId: $employeeId
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