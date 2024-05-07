export interface DeviceDto {
  id?: number;
  description?: string;
  typeId?: number;
  facilityId?: number;
  barcode?: string;
  serialNumber?: string;
  status?: string;
  active?: boolean;
  orderNumber?: number;
  retireDate?: Date;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
  deviceType?: string;
  deviceTypeId?: number;
  deviceSubtype?: string;
  reason?: string;
  facility?: string;
  typeKey?: string;
  locationType?: string;
  locationTypeId?: number;
  isInitialFreezer?: boolean;
  comments?: string;
  properties?: any;
}
