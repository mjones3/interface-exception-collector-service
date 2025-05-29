import { gql } from 'apollo-angular';
import { UseCaseResponseDTO } from '../../../../shared/models/use-case-response.dto';


export interface ShipmentHistoryDTO{
    id:number,
    shipmentId?:number,
    comments:string,
    createEmployeeId:string,
    createDate:string
}

export const FIND_SHIPMENT_HISTORY_BY_ID = gql<
    {
        findAllShipmentHistoryByShipmentId: ShipmentHistoryDTO;
    },
    {
        shipmentId: number;
    }
>`
    query findAllShipmentHistoryByShipmentId($shipmentId: ID!) {
        findAllShipmentHistoryByShipmentId(shipmentId: $shipmentId) {
            id
            createDate
            createEmployeeId
            comments
        }
    }
`;
