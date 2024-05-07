export interface ExternalTransferDto {
  id?: number;
  customerId: number;
  transferDate: string;
  externalOrderId?: string; //maxLength: 50
  locationId: number;
  createDate?: string;
  modificationDate?: string;
  externalTransferItems: ExternalTransferItemDto[];
}

export interface ExternalTransferItemDto {
  id?: number;
  externalTransferId?: number;
  inventoryId: number;
  unitNumber: string;
  productCode: string;
  createDate?: string;
  modificationDate?: string;
  customerIdFrom?: number;
}
