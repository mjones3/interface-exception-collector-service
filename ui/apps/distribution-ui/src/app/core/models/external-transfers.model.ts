import { ValidateRuleDto } from '@rsa/commons';

export const ADD_PRODUCT_TO_BATCH_VALIDATION_RULE = 'rul-0095-add-products-to-external-transfer-batch';

export interface TransferProduct {
  inventoryId: number;
  description?: string;
  productCode: string;
  unitNumber: string;
  originalDateShipped?: string;
  originallyShippedFrom?: string;
  originallyShippedTo?: string;
  lastTransferDate?: string;
  customerIdFrom?: number;
}

export interface AddProductRuleResult {
  externalTransferItems: [TransferProduct[]];
}

export interface AddProductRuleRequest extends ValidateRuleDto {
  unitNumber: string;
  inventoryIDList: number[];
  transferDate: string;
}
