export interface EventManagementDto {
  id?: number;
  typeKey: string;
  statusKey?: string;
  statusModificationDate?: string;
  discoveryDate: string;
  dueDate: string;
  reasonKey: string;
  description: string;
  comments?: string;
  locationId?: number;
  locationName?: string;
  employeeId?: string;
  clientTimezone?: string;
  createDate?: string;
  modificationDate?: string;
  referenceNumbers?: string[];
  unitNumbers?: string[];
}

export enum EVENT_MANAGEMENT_CRITERIA_TYPES {
  PRODUCT_CRITERIA = 'PRODUCT_CRITERIA',
  DONOR_CRITERIA = 'DONOR_CRITERIA',
}

export interface EventManagementCriteriaDto {
  id?: number;
  eventManagementId: number;
  type: EVENT_MANAGEMENT_CRITERIA_TYPES;
  identifierType: string;
  criteriaJson: string;
  employeeId?: string;
  createDate?: string;
  modificationDate?: string;
  clientTimezone?: string;
  inventoryIds?: number[];
  donorIds?: number[];
  message?: string;
  messageType?: string;
}

export interface EventManagementReviewDto {
  id?: number;
  eventManagementId: number;
  typeKey: string;
  comments: string;
  employeeId?: string;
  clientTimezone?: string;
  createDate?: string;
}

export interface EventManagementReviewTypesDto {
  id?: number;
  typeKey: string;
}

export interface EventManagementProductActionDto {
  id?: number;
  eventManagementId: number;
  eventManagementCriteriaId: number;
  actionKey: string;
  reasonKey: string;
  sourceKey: string;
  statusKey: string;
  comments?: string;
  employeeId?: string;
  clientTimezone?: string;
  createDate?: string;
  modificationDate?: string;
  inventoryIds: number[];
  quarantineIds: number[];
}

export interface ProcessingStatusDto {
  id?: number;
  criteriaId: number;
  totalPending: number;
  totalFailed: number;
  totalSuccessful: number;
  totalItems: number;
  status: string;
  entity: string;
}

export interface EventManagementDonorDetailsDto {
  id?: number;
  eventManagementCriteriaId?: number;
  firstName?: string;
  lastName?: string;
  unitNumbers?: string;
  poolNumber?: string;
}

export interface EventManagementProductDetailsDto {
  id?: number;
  currentFacilityId?: number;
  eventManagementCriteriaId?: number;
  unitNumber?: string;
  productCode?: string;
  productDescriptionKey?: string;
  status?: string;
  drawDate?: string;
  expirationDate?: string;
  poolNumber?: string;
  aboRh?: string;
  productActionRequested?: string;
}
