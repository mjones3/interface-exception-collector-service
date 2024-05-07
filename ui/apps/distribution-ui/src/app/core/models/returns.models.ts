export const RETURN_REASON_TYPE_KEY = 'return-reason.label';
export const RETURNS_TRANSIT_TIME_ZONE = 'RETURNS_TRANSIT_TIME_ZONE';
export const RETURNS_INSPECTION_STATUS = 'RETURNS_INSPECTION_STATUS';
export const QUARANTINE_CONSEQUENCE_TYPE = 'ADD_INVENTORY_QUARANTINE';
export const RETURN_TO_INVENTORY_CONSEQUENCE_TYPE = 'RETURN_TO_INVENTORY';
export const RETURN_INFORMATION_VALIDATION_RULE = 'rul-0094-return-import-validation';
export const ADD_PRODUCT_TO_BATCH_VALIDATION_RULE = 'add-products-to-return-batch';
export const RESULT_PROPERTY_INSPECTION_FIELD = 'visual-inspection-field.label';

export interface ReturnItem {
  unitNumber: string;
  returnItemConsequences: ReturnItemConsequence[];
  rareDonor: boolean;
  quarantined: boolean;
  inventoryId: number;
  isbtProductCode: string;
  expired: boolean;
  positiveBacterialTestingFollowUp: boolean;
}

export interface ReturnItemConsequence {
  consequenceType: string;
  consequenceReasonKey: string;
  productCategory?: string;
  resultValue?: string;
  resultProperty?: string;
}
