import { IrradiationInventoryDto } from './irradiation-inventory.dto';

export interface IrradiationDto {
  id?: number;
  irradiator?: string;
  locationId: number;
  timeRemoved?: string;
  timeOutDate?: string;
  createEmployeeId?: string;
  closeEmployeeId?: string;
  closeDate?: string;
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  inventories: IrradiationInventoryDto[];
}
