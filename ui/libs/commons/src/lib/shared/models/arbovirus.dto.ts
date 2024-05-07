export interface ArbovirusDto {
  id?: number;
  caseId: string;
  typeId: number;
  triggerUnitNumber: string;
  triggerZipCode: string;
  discoveryDate: string;
  sourceKey: string;
  reviewDecisionKey: string;
  noCaseReasonKey: string;
  statusKey: string;
  openDate: string;
  openedByEmployeeId: string;
  closeDate: string;
  closedByEmployeeId: string;
  clientTimezone: string;
  createDate: string;
  modificationDate: string;
  supplementalZipCodes: number[] | string[];
  triggerTestTypeIds: number[];
  arbovirusCaseHistories: ArbovirusCaseHistoryDto[];
}

export interface ArbovirusTypeDto {
  id: number;
  displayName: string;
}

export interface ArbovirusCaseHistoryDto {
  id?: number;
  arbovirusId?: any;
  supplementalZipCodes?: string;
  comments: string;
  reviewedByEmployeeId?: string;
  clientTimezone?: any;
  createDate?: string;
  modificationDate?: string;
}

export interface ArbovirusPotentialNoCaseDto {
  noCaseReasonKey: string;
  comments: string;
}

export interface ArbovirusOpenCaseDto {
  openedDate?: string;
  comments?: string;
  supplementalZipCodes?: string[];
}
