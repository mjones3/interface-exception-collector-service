export interface CentrifugeDto {
  id?: number;
  centrifugeTypeId: number;
  processDeviceId: number;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
  inventories?: any[];

  [key: string]: any;
}
