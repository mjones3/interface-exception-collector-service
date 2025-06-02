import { gql } from "apollo-angular";
import { UseCaseResponseDTO } from "app/shared/models/use-case-response.dto";
import { RecoveredPlasmaShipmentResponseDTO } from "../../models/recovered-plasma.dto";


export interface ModifyShipmentRequestDTO{
    shipmentId: number,
    customerCode:string,
    productType:string,
    transportationReferenceNumber:string,
    shipmentDate?:string,
    cartonTareWeight?:number,
    modifyEmployeeId:string,
    comments:string
}

export const MODIFY_RECOVERED_PLASMA_SHIPMENT = gql<
    { modifyShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO> },
    ModifyShipmentRequestDTO
>`
    mutation modifyShipment(
        $customerCode: String!
        $productType: String!
        $cartonTareWeight: Float
        $shipmentDate: Date
        $transportationReferenceNumber: String
        $modifyEmployeeId: String!
        $comments: String!
        $shipmentId: ID!
    ) {
        modifyShipment(
            modifyShipmentRequest: {
                customerCode: $customerCode
                productType: $productType
                cartonTareWeight: $cartonTareWeight
                shipmentDate: $shipmentDate
                transportationReferenceNumber: $transportationReferenceNumber
                modifyEmployeeId: $modifyEmployeeId
                shipmentId: $shipmentId
                comments: $comments
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
