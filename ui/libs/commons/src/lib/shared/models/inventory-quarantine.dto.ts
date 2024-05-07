export interface InventoryQuarantineDto {
  id?: number;
  createDate: Date;
  donationId?: number;
  inventoryId: number;
  quarantineInventoryId?: number;
  quarantined?: boolean;
}
