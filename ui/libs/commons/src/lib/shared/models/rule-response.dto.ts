import { InventoryResponseDto } from './inventory-response.dto';
import { NotificationDto } from './notification.dto';

export interface RuleResponseDto {
  ruleCode?: string;
  _links?: any;
  results?: any;
  notifications?: NotificationDto[];
}

export interface PlasmaApheresisRuleResultDto {
  result: InventoryResponseDto[];
}
