import { ShipFromDTO, ShipToDTO } from './ship.model';
import { ShipmentItemPackedDTO } from './shipment-info.dto';

export interface PackingListLabelDTO {
    shipmentId: number;
    orderNumber: number;
    orderIdBase64Barcode: string;
    shipmentIdBase64Barcode: string;
    dateTimePacked: string;
    packedBy: string;
    enteredBy: string;
    quantity: number;
    shipFrom: ShipFromDTO;
    shipTo: ShipToDTO;
    distributionComments: string;
    packedItems: ShipmentItemPackedDTO[];
}
