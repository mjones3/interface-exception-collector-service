export interface DeviceReportDto {
  id: number;
  description: string;
  locationId: number;
  locationName: string;
  locationTypeId: number;
  locationTypeDescriptionKey: string;
  parentDeviceId: number;
  parentDeviceDescriptionKey: string;
  deviceTypeId: number;
  deviceTypeDescriptionKey: string;
  bloodCenterId: string;
  serialNumber: string;
  status: string;
}
