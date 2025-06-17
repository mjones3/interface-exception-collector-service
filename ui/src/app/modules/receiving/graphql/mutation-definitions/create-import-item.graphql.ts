import { gql } from "apollo-angular";
import { UseCaseResponseDTO } from "app/shared/models/use-case-response.dto";
import {
    AddImportItemRequestDTO,
    CompleteImportRequestDTO,
    CreateImportResponsetDTO
} from '../../models/product-information.dto';
import { CartonDTO } from '../../../recovered-plasma-shipment/models/recovered-plasma.dto';


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


export const FIND_IMPORT_BY_ID = gql<
    {
        findImportById: UseCaseResponseDTO<CreateImportResponsetDTO>;
    },
    {
        importId: number;
    }
>`
    query findImportById($importId: ID!) {
        findImportById(importId: $importId) {
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

export const COMPLETE_IMPORT = gql<
    { completeImport: UseCaseResponseDTO<CreateImportResponsetDTO> },
    CompleteImportRequestDTO
>`
    mutation completeImport(
        $importId:ID!
        $completeEmployeeId:String!
    ) {
        completeImport(
            completeImportRequest: {
                importId: $importId
                completeEmployeeId: $completeEmployeeId
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
