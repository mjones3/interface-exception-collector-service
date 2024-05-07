export interface PqcTestResultDto {
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  id?: number;
  pqcTestCriteriaId: number;
  pqcTestTypeId?: number;
  pqcTestTypeKey?: string;
  locationId: number;
  deviceId?: number;
  inventoryId?: number;
  batchId?: number;
  taskKey?: string;
  resultKey: string;
  resultValue: string;
  finalResult?: string;
  status: string;
  entryEmployeeId?: string;
  reviewEmployeeId?: string;
  reviewTypeId: number;
  reviewDate?: string;
  items?: PqcTestResultItemDto[];
  comments?: string;
  pqcTestType?: PqcTestTypeDto;
  donationId?: number;
}

export interface PqcTestResultDetailsDto extends PqcTestResultDto {
  unitNumber?: string;
  description?: string;
  productCode?: string;
  entryUser?: boolean;
  doubleBlindReview?: boolean;
  readyToReview?: boolean;
}

export interface PqcTestResultItemDto {
  id?: number;
  pqcTestResultId?: number;
  name: string;
  key: string;
  value: string;
  orderNumber: number;
}

export interface PqcTestTypeDto {
  orderNumber: number;
  active?: boolean;
  createDate?: string;
  modificationDate?: string;
  id?: number;
  descriptionKey: string;
  reviewType: string;
  layout?: string;
  maxSampleAge?: string;
  unitOfMeasure?: string;
  decimalPlace?: number;
  automaticSampling?: boolean;
  deviceTypes?: number[];
  locations?: number[];
  requirements?: PqcTestTypeRequirementDto[];
  doubleBlindReview?: boolean;
}

export interface PqcTestTypeRequirementDto {
  requirementType: string;
  requirementValue: string;
  orderNumber: number;
}

export interface PqcTestFieldDto {
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
  id?: number;
  descriptionKey: string;
  testCriteriaId: number;
  fieldName: string;
  fieldType: string;
  testFieldOptions?: PqcTestFieldOptionDto[];
}

export interface PqcTestFieldOptionDto {
  optionType: string;
  optionValue: string;
  orderNumber: number;
}

export interface PqcBatchDto {
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  id?: number;
  pqcTestTypeId: number;
  locationId: number;
  employeeId?: string;
  deviceId: number;
}

export interface PqcMeanTestResultDto {
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  id?: number;
  pqcTestTypeId: number;
  locationId: number;
  startDate?: string;
  endDate?: string;
  resultValue?: string;
  employeeId?: string;
  month: Month;
  year: number;
}

export interface DoubleBlindResponseDto {
  status?: string; //HttpStatus
  error?: string;
  nextLink?: string;
  message?: string;
  pqcTestResults?: PqcTestResultDto[];
}

export interface PqcTestRequestDto {
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  id?: number;
  sampleId?: number;
  inventoryId: number;
  pqcTestTypeId: number;
  pqcTestResultId?: number;
  taskKey: string;
  employeeId?: string;
  expirationDate?: string;
  usageDate?: string;
}

export interface PqcTestCriteriaDto {
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
  id?: number;
  testTypeId: number;
  calculations?: PqcTestCriteriaCalculationDto[];
  consequences?: PqcTestCriteriaConsequenceDto[];
  requirements?: PqcTestCriteriaRequirementDto[];
}

export interface PqcTestCriteriaCalculationDto {
  id?: number;
  pqcTestCriteriaId: number;
  type: string;
  value: string;
  orderNumber: number;
  active: boolean;
}

export interface PqcTestCriteriaConsequenceDto {
  id?: number;
  pqcTestCriteriaId: number;
  acceptable: string;
  resultType: string;
  resultValue: string;
  consequenceType?: string;
  consequenceValue?: string;
  orderNumber: number;
  active: boolean;
}

export interface PqcTestCriteriaRequirementDto {
  id?: number;
  pqcTestCriteriaId: number;
  type: string;
  value: string;
  orderNumber: number;
  active: boolean;
}

export interface PqcTestCalculationRequest {
  inventoryId?: number;
  parentInventoryId?: number;
  testCriteriaId?: number;
  testTypeId?: number;
  testTypeKey?: string;
  resultTestTypeId?: number;
  locationId: number;
  testFields?: PqcTestCalculationField[];
  year?: number;
  month?: Month;
}

export interface PqcTestCalculationField {
  fieldName?: string;
  fieldValue?: string;
}

export interface PqcTestCalculationResponse {
  value?: string;
  parameters?: { [key: string]: object };
}

export interface PqcSampleTransactionDto {
  donationId: number;
  taskKey: string;
  inventoryId: number;
  sampleVerification: boolean;
  testDescriptionKeyList?: string[];
  quarantineReasonKeyList?: string[];
  positiveBacterialFlagKey?: string;
  facilityId: number;
  nextLink?: string;
  options: string[];
  fieldsConfigured?: boolean;
}

export enum Month {
  JANUARY = 'JANUARY',
  FEBRUARY = 'FEBRUARY',
  MARCH = 'MARCH',
  APRIL = 'APRIL',
  MAY = 'MAY',
  JUNE = 'JUNE',
  JULY = 'JULY',
  AUGUST = 'AUGUST',
  SEPTEMBER = 'SEPTEMBER',
  OCTOBER = 'OCTOBER',
  NOVEMBER = 'NOVEMBER',
  DECEMBER = 'DECEMBER',
}
