import { ShipFromDTO, ShipToDTO } from './ship.model';

export interface ShipmentItemPackedDTO {
  shipmentItemId: number,
  inventoryId: number,
  unitNumber: string,
  productCode: string,
  aboRh: string,
  productDescription: string,
  productFamily: string,
  expirationDate: string,
  collectionDate: string,
}

export interface PackingListLabelDTO {
  shipmentId: number,
  orderNumber: number,
  orderIdBase64Barcode: string,
  shipmentIdBase64Barcode: string,
  dateTimePacked: string,
  packedBy: string,
  enteredBy: string,
  quantity: number,
  shipFrom: ShipFromDTO,
  shipTo: ShipToDTO,
  distributionComments: string,
  packedItems: ShipmentItemPackedDTO[],
}
