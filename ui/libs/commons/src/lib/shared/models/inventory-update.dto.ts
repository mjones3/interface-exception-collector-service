import { InventoryPropertyDto } from './inventory-property.dto';
import { InventoryDto } from './inventory.dto';

export interface InventoryUpdateDto {
  id?: number;
  parentId?: number;
  productCode?: string;
  descriptionKey?: string;
  donationId?: number;
  processIndex?: string;
  status?: string;
  discardDate?: Date;
  facilityId?: number;
  currentFacilityId?: number;
  properties?: any;
}

export interface InventoryParentUpdateDto {
  parentProcessIndex?: string;
  parentStatus?: string;
  parentInventoryProperties?: InventoryPropertyDto;
  inventories?: InventoryDto[];
}
