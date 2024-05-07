export const RareDonorAuditDtoValues = {
  LOG_LEVEL: 'INFO',
  ACTION: 'rare-donor.acknowledgement',
  TYPE: 'inventory',
};

export interface AuditTrailDto {
  id?: number;
  action: string;
  employeeId: string;
  comments?: string;
  createDate: Date;
  logLevel?: string;
  changes?: AuditValueChangeEntityDto[];
  references?: AuditReferenceEntityDto[];
}

export interface AuditValueChangeEntityDto {
  id?: number;
  descriptionKey: string;
  oldValue?: string;
  newValue?: string;
  isLanguageKey: boolean;
}

export interface AuditReferenceEntityDto {
  id?: number;
  type: string;
  value: string;
}
