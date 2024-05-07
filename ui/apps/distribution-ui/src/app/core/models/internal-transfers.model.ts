import { NotificationDto, ProductSelectionItemDto, TransferReceiptDto } from '@rsa/commons';

export type ProductSelectionItemModel = ProductSelectionItemDto;
export interface ProductSelectionRuleModel {
  notifications?: NotificationDto[];
  ruleCode?: string;
  results?: {
    notificationStatus?: number[];
    inventories?: ProductSelectionItemModel[][];
  };
}

export interface ProductionSelectionRuleInputModel {
  ruleName: string;
  ruleInputs: {
    unitNumber: string;
    transferDate: string;
    facilityId: number;
    status: string;
  };
}

export type TransferCompleteModel = Partial<TransferReceiptDto>;
