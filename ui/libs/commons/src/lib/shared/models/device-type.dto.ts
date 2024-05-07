export interface DeviceTypeDto {
  id?: number;
  parentId?: number;
  descriptionKey?: string;
  batchable?: boolean;
  active?: boolean;
  orderNumber?: number;
  createDate?: Date;
  modificationDate?: Date;
  childrens?: DeviceTypeDto[];
  properties?: any;
}
