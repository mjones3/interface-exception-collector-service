import { NotificationDto } from '@shared';

export interface RuleResponseDTO<TResults = any> {
    ruleCode?: string;
    notifications?: NotificationDto[];
    _links?: Record<string, string>;
    results?: TResults;
}

export interface NotificationModalDTO {
    data?: RuleResponseDTO;
}

export interface NotificationTabDTO<T = any> {
    tabName?: string;
    inventoryResponse?: T;
}

export interface InventoryResponseDTO<T = any> {
    inventoryNotificationsDTO?: T[];
    inventoryResponseDTO?: T;
}
