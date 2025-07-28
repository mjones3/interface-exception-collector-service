import { gql } from "apollo-angular";
import { UseCaseResponseDTO } from "app/shared/models/use-case-response.dto";


export interface ValidateBarcodeRequestDTO {
    temperatureCategory: string,
    barcodeValue: string,
    barcodePattern: string
}

export const vALIDATE_BAR_CODE = gql<
    { validateBarcode: UseCaseResponseDTO<any> },
    ValidateBarcodeRequestDTO
>`
    query validateBarcode(
        $temperatureCategory:String!
        $barcodeValue:String!
        $barcodePattern:String!
    ) {
        validateBarcode(validateBarcodeRequest: {
            temperatureCategory: $temperatureCategory,
            barcodeValue: $barcodeValue,
            barcodePattern: $barcodePattern
        }) {
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
