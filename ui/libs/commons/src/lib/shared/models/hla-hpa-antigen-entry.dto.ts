import { EmployeeDto } from './employee.dto';
import { NoteDto } from './note.dto';

export interface HlaHpaAntigenEntryDTO {
  id?: number;
  donationId: number;
  unitNumber: string;
  donorId: number;
  hlaHpaAntigenTypeId: number;
  locationId?: number;
  hlaHpaAntigenBatchId?: number;
  statusKey: string;
  source?: string;
  comments?: string;
  result: string;
  reviewResult?: string;
  employeeId: string;
  reviewEmployeeId?: string;
  reviewDate?: Date;
  inactivatedEmployeeId?: string;
  inactivatedDate?: Date;
  createDate: Date;
  modificationDate: Date;
  reviewDateTimezone?: string;
  inactivatedDateTimezone?: string;
  createDateTimezone?: string;
  modificationDateTimezone?: string;
  deleteDateTimezone?: string;
}

export interface HlaHpaAntigenTypeDTO {
  id?: number;
  descriptionKey: string;
  orderNumber: number;
  active: boolean;
  createDate: Date;
  modificationDate: Date;
}

export interface HlaHpaAntigenTypeEntryDTO {
  id: number;
  hlaHpaAntigenTypeId: number;
  valueKey: string;
  value: string;
}

export interface HlaHpaAntigenReportDTO {
  id: number;
  batchNumber: number;
  unitNumber: string;
  locationId: number;
  locationName: string;
  statusKey: string;
  entryDate: Date;
  donorFirstName: string;
  donorLastName: string;
  hlaHpaAntigenTypeDescriptionKey: string;
  hlaHpaAntigenTypeId: number;
}

export interface HlaHpaAntigenReportCriteria {
  batchNumber: number;
  unitNumber: string;
  locationId: number;
  'statusKey.in': string;
  'entryDate.greaterThanOrEqual': Date;
  'entryDate.lessThanOrEqual': Date;
  donorFirstName: string;
  donorLastName: string;
  hlaHpaAntigenTypeId: number;
}

export interface HlaHpaAntigenReportDetailCriteria {
  unitNumber?: string;
  hlaHpaAntigenTypeId: number;
  page: number;
  size: number;
  sort: string;
  statusKey?: string;
  donorId?: number;
  'unitNumber.notIn'?: string;
}

export interface HlaHpaAntigenEntryCommentsDTO {
  comments?: NoteDto[];
  totalComments?: number;
}

export interface HlaHpaAntigenEntryWithCommentsDTO {
  entry: HlaHpaAntigenEntryDTO;
  comments?: HlaHpaAntigenEntryCommentsDTO;
  employees?: EmployeeDto[];
}

export interface HlaHpaAntigenEntryInactivateDTO {
  statusKey: string;
  comments: string;
  inactivatedReasonKey: string;
  timeZone: string;
}
