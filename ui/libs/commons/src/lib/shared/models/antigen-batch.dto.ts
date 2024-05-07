export interface AntigenBatchDto {
  id?: number;
  locationId: number;
  batchNumber?: string;
  employeeId?: string;
  batchEntries?: AntigenBatchEntryDto[];
  createDate?: string;
  createDateTimezone?: string;
  modificationDateTimezone?: string;
}

export interface AntigenBatchEntryDto {
  id?: number;
  donationId: number;
  unitNumber: string;
  orderNumber: number;
}

export interface Workstation {
  id: number;
  name: string;
}

export interface TestingMethodTranslate {
  optionValue: string;
  description: string;
}
