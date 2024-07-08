export interface ShipToDTO {
  customerCode: number,
  customerName: string,
  department: string,
  addressLine1: string,
  addressLine2: string,
  addressComplement: string
  phoneNumber: string,
}

export interface ShipFromDTO {
  bloodCenterCode: string,
  bloodCenterName: string,
  bloodCenterBase64Barcode: string,
  bloodCenterAddressLine1: string,
  bloodCenterAddressLine2: string,
  bloodCenterAddressComplement: string,
  phoneNumber: string,
}
