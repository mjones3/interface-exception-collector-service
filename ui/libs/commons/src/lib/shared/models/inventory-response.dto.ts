import { NotificationDto } from './notification.dto';

export interface InventoryResponseDto {
  id?: number;
  parentId?: number;
  productCode?: string;
  descriptionKey?: string;
  shortDescriptionKey?: string;
  processIndex?: string;
  donationId?: number;
  status?: string;
  isQuarantine?: boolean;
  properties?: any;
  discardDate?: Date;
  deleteDate?: Date;
  createDate?: Date;
  notifications?: NotificationDto[];
  icon?: string;
  unitNumber?: string;
  currentFacilityId?: number;
  productFields?: any;
}
