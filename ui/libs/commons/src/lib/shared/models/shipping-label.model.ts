import { ShipFromDTO, ShipToDTO } from './ship.model';

export type ShippingLabelDTO = {
  shipmentId: number,
  orderNumber: number,
  orderIdBase64Barcode: string,
  shipmentIdBase64Barcode: string,
  shipTo: ShipToDTO,
  shipFrom: ShipFromDTO,
  dateTimePacked: string
}
