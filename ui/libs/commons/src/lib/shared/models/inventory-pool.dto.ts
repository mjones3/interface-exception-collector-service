export interface InventoryPoolDto {
  id?: number;
  pooledInventoryId: number;
  childInventoryId: number;
  type: string;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
  unitNumber?: string;

  [key: string]: any;
}
