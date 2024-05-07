export interface ClosedBatchResponseDto {
  data: ClosedBatchDto[];
}

export interface ClosedBatchDto {
  id?: number;
  productCode?: string;
  isbt?: string;
  labelingCollectionTypeCode?: string;
  unitNumber?: string;
  donationId?: number;
  inventoryId?: number;
  timeRemovedFromStorage?: string;
}
