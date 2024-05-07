import { InventoryResponseDto } from './inventory-response.dto';

export interface InventoriesResponseDto {
  inventories: InventoryResponseDto[];
  notifications: Notification;
}
