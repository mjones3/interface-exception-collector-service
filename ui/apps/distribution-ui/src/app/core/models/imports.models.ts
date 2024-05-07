import { ValidateRuleDto } from '@rsa/commons';

export const IMPORTS_TRANSIT_TIME_ZONE = 'RETURNS_TRANSIT_TIME_ZONE';
export const IMPORTS_INSPECTION_STATUS = 'RETURNS_INSPECTION_STATUS';
export const QUARANTINE_CONSEQUENCE_TYPE = 'ADD_INVENTORY_QUARANTINE';
export const RETURN_TO_INVENTORY_CONSEQUENCE_TYPE = 'RETURN_TO_INVENTORY';
export const IMPORTS_INFORMATION_VALIDATION_RULE = 'rul-0094-return-import-validation';
export const RESULT_PROPERTY_INSPECTION_FIELD = 'visual-inspection-field.label';
export const IMPORTS_BLOOD_TYPES = 'IMPORTS_BLOOD_TYPES';
export const ADD_PRODUCT_TO_BATCH_VALIDATION_RULE = 'add-products-to-import-batch';
export const LICENSE_STATUS = 'LICENSE_STATUS';
export const CMV_STATUS = 'CMV_STATUS';
export const HBS_NEGATIVE = 'HBS_NEGATIVE';
export const PENDING_STATUS = 'PENDING';
export const PATIENT_TYPE = 'IMPORTED';
export const IN_PROCESS_STATUS = 'IN_PROCESS';
export const COMPLETED_STATUS = 'COMPLETED';

export type ImportItemAttributeType =
  | 'licensed.label'
  | 'unlicensed.label'
  | 'cmv-negative.label'
  | 'cmv-positive.label'
  | boolean;

export interface ImportItem {
  id?: number;
  unitNumber?: string;
  bloodType?: string;
  isbtProductCode?: string;
  descriptionKey?: string;
  expirationDate?: string;
  patientRecord?: boolean;
  patient?: Patient;
  facilityIdentification?: ImportFacilityIdentification;
  itemAttributes?: ImporItemAttribute[];
  returnItemConsequences?: ImportItemConsequence[];
}

export interface ImporItemAttribute {
  propertyKey: string;
  propertyValue: ImportItemAttributeType;
}

export interface ImportItemConsequence {
  consequenceType: string;
  consequenceReasonKey: string;
  productCategory?: string;
  resultValue?: string;
  resultProperty?: string;
}

export interface Patient {
  id?: number;
  firstName?: string; //maxLength: 50
  lastName?: string; //maxLength: 50
  dob?: string;
  abo?: string;
  rh?: string;
}

export interface ImportFacilityIdentification {
  id?: number;
  fin: string;
  name: string;
  licenseNumber?: string;
  registrationNumber: string;
  city: string;
  state?: string;
  postalCode?: string;
  country: string;
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
}

export interface ImportItemConsequence {
  consequenceType: string;
  consequenceReasonKey: string;
  productCategory?: string;
  resultValue?: string;
  resultProperty?: string;
}

export interface Product {
  productCode: string;
  descriptionKey: string;
}

export interface AddProductRuleResult {
  importFacilityIdentification: [ImportFacilityIdentification[]];
  importItem: ImportItem[];
  product: Product[];
}

export interface AddProductRuleRequest extends ValidateRuleDto {
  unitNumber: string;
  bloodType: string;
  isbtProductCode: string;
  expirationDate: string;
  facilityId: number;
  familyCategory: string;
  itemAttributes: ImporItemAttribute[];
}
