import { IrradiationDto } from './irradiation.dto';

export interface IrradiationInventoryDto {
  id?: number;
  irradiation?: IrradiationDto;
  inventoryId: number;
  isbt?: string;
  status?: string;
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
}
