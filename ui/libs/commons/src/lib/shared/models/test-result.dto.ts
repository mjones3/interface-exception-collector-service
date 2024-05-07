export interface TestResultDto {
  id?: number;
  referenceId?: number;
  unitNumber: string;
  result: string;
  type?: string;
  testTypeId?: number;
  locationId?: number;
  createEmployeeId?: string;
  createComments?: string;
  createReasonKey?: string;
  inactivateComments?: string;
  inactivateReasonKey?: string;
  confirmEmployeeId?: string;
  confirmDate?: string;
  confirmComments?: string;
  pendingDeferral?: boolean;
  reviewResult?: string;
  exception?: boolean;
  screenName?: string;
  interfaceResult?: string;
  deleteDate?: string;
  createDate: string;
  status?: string;
}

export interface TestResultExceptionDto {
  id?: number;
  referenceId: number;
  unitNumber: string;
  result: string;
  testTypeId: number;
  donationQuarantineId?: number;
  drawDate?: string;
  aboRh?: string;
  status?: string;
  resolutionDate?: string;
  resolvedEmployeeId?: string;
  locationId?: number;
  comments?: string;
  resolutionReasonKey?: string;
  resolutionType?: string;
  exceptionType?: string;
  deleteDate?: string;
  createDate?: string;
  modificationDate?: string;
  activeResult?: string;
}

export interface ResolveTestResultExceptionDto {
  aboRh: string;
  locationId: number;
  comments?: string;
  reasonKey: string;
  unitDispositionKey: string;
  finalResult: string;
}

export interface ResolveTestResultExceptionResponseDto {
  statusCode: string;
  notificationType: string;
  message: string;
  errorReason: string;
}

export interface TestGroupDto {
  id?: number;
  descriptionKey: string;
  testTypeId?: number;
  result?: string;
  startDate: string;
  endDate: string;
  requirements?: TestTypeDto[];
  tests?: string[];
  orderNumber: number;
  active: boolean;
  modificationDate: string;
}

export interface TestGroupResponseDto {
  testTypeKey: string;
  testTypeId: number;
  lastTestResultTestTypeIdStandardLink?: number;
  lastTestResultResultStandardLink?: string;
  testTypeIdTestGroupConfirmatoryLink?: number;
  resultTestGroupConfirmatoryLink?: string;
  status?: string;
  lastTestResultValue: TestResultDto;
  details?: TestResultDto[];
}

export interface TestTypeEntryDto {
  id: number;
  testTypeId: number;
  type: string;
  value: string;
  valueKey: string;
  orderNumber: number;
  active: boolean;
  modificationDate: string;
}

export interface TestTypeDto {
  id: number;
  descriptionKey: string;
  reviewType: TestTypeReviewType;
}

export interface InactivateTestResultDto {
  inactivateComments: string;
  inactivateReasonKey: string;
}

export enum TestTypeReviewType {
  SYSTEM = 'SYSTEM',
  USER = 'USER',
}
