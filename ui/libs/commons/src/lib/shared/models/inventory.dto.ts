export enum InventoryProcessIndex {
  LABEL_VOIDED = 'label-voided',
  LABELED = 'labeled',
  LABEL_PRINTED = 'label-printed',
  DONE = 'done',
}

export interface InventoryDto {
  id?: number;
  parentId?: number;
  productCode?: string;
  descriptionKey?: string;
  isbtCode?: string;
  icon?: string;
  donationId?: number;
  processIndex?: string;
  status?: string;
  discardDate?: Date;
  facilityId?: number;
  currentFacilityId?: number;
  properties?: object;
  labelType?: any;
  relabel?: any;
  quarantine?: boolean;
  createDate?: Date;
  modificationDate?: Date;
  isQuarantine?: boolean;
}

//IMPORTS
export interface ImportDto {
  id?: number;
  productCategory: string;
  comments?: string; //maxLength: 1000
  transitStartDateTime?: string;
  transitEndDateTime?: string;
  transitTimezone?: string;
  totalTransitTime?: string;
  transitTimeResultKey?: string;
  shipmentInspectKey: string;
  temperature?: string;
  locationId: number;
  createDate?: string;
  modificationDate?: string;
  importItems: ImportItemDto[];
}

export interface ImportItemDto {
  id?: number;
  importId?: number;
  unitNumber: string;
  productCode: string;
  bloodType: string;
  productConsequenceKey: string;
  licenseStatus: string;
  expirationDate: string;
  licenseNumber?: string;
  registrationNumber?: string;
  status: string;
  patientId?: number;
  createDate?: string;
  modificationDate?: string;
  importItemAttributes: ImporItemAttributeDto[];
  importItemConsequences: ImportItemConsequenceDto[];
}

export interface ImporItemAttributeDto {
  id?: number;
  importItemId?: number;
  propertyKey: string;
  propertyValue: string;
  createDate?: string;
  modificationDate?: string;
}

export interface ImportItemConsequenceDto {
  id?: number;
  importItemId?: number;
  itemConsequenceType: string;
  itemConsequenceReasonKey?: string;
  createDate?: string;
  modificationDate?: string;
}

export interface ImportStatusDto {
  id?: number;
  totalPending?: number;
  totalFailure?: number;
  totalSuccess?: number;
  totalItems?: number;
  status?: string;
  failures?: ImportItemDto[];
}

//EVENT MANAGEMENT
export interface ProductStatusDto {
  inventoryId: number;
  statuses: string[];
}
