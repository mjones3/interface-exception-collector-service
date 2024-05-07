export interface HlaHpaAntigenBatchDto {
  id?: number;
  locationId?: number;
  employeeId?: string;
  batchEntries?: HlaHpaAntigenBatchEntryDto[];
  hlaHpaAntigenBatchDonations?: HlaHpaAntigenBatchDonationDto[];
  createDate?: string;
  createDateTimezone?: string;
  modificationDateTimezone?: string;
  batchDonations?: string[];
}

export interface HlaHpaAntigenBatchEntryDto {
  id?: number;
  donationId?: number;
  unitNumber?: string;
  orderNumber?: number;
}

export interface HlaHpaAntigenBatchDonationDto {
  hlaHpaAntigenBatchId?: number;
  unitNumber: string;
  orderNumber?: number;
}

export interface HlaHpaAntigenTypeDto {
  id: number;
  optionValue: string;
  descriptionKey: string;
  orderNumber: number;
  active: boolean;
  createDate: string;
  modificationDate: string;
}

export interface ExportHlaHpaAntigenTypeEntryDto {
  id?: number;
  hlaHpaAntigenTypeId: number;
  valueKey: string;
  value: string;
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
}

export interface HlaOrHpaEntryDto {
  antigen: string;
  dateOfTesting?: string;
  result?: string;
  resultStatus?: string;
  historicalResult?: string;
  testType?: string;
  resultOptions?: string[];
  unitNumber?: string;
  hlaHpaAntigenTypeId?: number;
  statusKey?: string;
  source?: string;
}
export enum TestTypes {
  SELECTABLE = 'Selectable',
  REGULAR_ENTRY = 'Regular Entry',
  DOUBLE_BLIND = 'Double Blind',
  FELT = 'Felt',
}
export interface LastEntryDto {
  donationId: number;
  unitNumber: string;
  donorId: number;
  testType: string;
}
