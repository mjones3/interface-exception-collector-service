export type ShipToDTO = {
  customerCode: number,
  customerName: string,
  department: string,
  addressLine1: string,
  addressLine2: string,
  addressComplement: string
}

export type ShipFromDTO = {
  bloodCenterCode: string,
  bloodCenterName: string,
  bloodCenterAddressLine1: string,
  bloodCenterAddressLine2: string,
  bloodCenterAddressComplement: string
}

export type ShipmentItemPackedDTO = {
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

export type PackingListLabelDTO = {
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
