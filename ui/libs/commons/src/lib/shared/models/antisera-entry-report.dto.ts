import { WorkstationDto } from './workstation.dto';

export interface AntiseraEntryReportsDto {
  id?: number;
  locationId?: number;
  antigen?: string;
  manufacturer?: string;
  expirationDate?: Date;
  lotNumber?: string;

  negativeResultInitialRead?: string;
  negativeResultFinalRead?: string;
  positiveResultInitialRead?: string;
  positiveResultFinalRead?: string;
  checkCell?: string;

  qcInterpretation?: string;
  status?: string;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
  clientTimeZone?: string;
  reagentDescriptionKey: string;
  reagentId: number;
  entryId: number;
}

export interface AntiseraEntriesDto {
  clientTimeZone?: string;
  comments?: string;
  createDate?: Date;
  deleteDate?: Date;
  downtimeEntry?: boolean;
  durationDate?: Date;
  entryControls?: AntiseraEntryReagentsDto[];
  entryDate?: Date;
  entryEmployeeId?: string;
  entryWorkstations?: WorkstationDto[];
  expirationDate?: Date;
  formattedDurationTime?: string;
  id?: number;
  locationId?: number;
  lotNumber?: string;
  manufacturerId?: number;
  modificationDate?: Date;
  qcInterpretation?: string;
  qcType?: string;
  reagentId?: number;
  reviewDate?: Date;
  reviewEmployeeId?: number;
  status?: string;
}

export interface AntiseraEntryReagentsDto {
  cellNumber?: string;
  clientTimeZone?: string;
  createDate?: Date;
  deleteDate?: Date;
  entryControlResults?: AntiseraControlResultsDto;
  expirationDate?: Date;
  id?: number;
  lotNumber?: string;
  manufacturerId?: number;
  modificationDate?: Date;
  qcControlType?: string;
  qcEntryId?: number;
}

export interface AntiseraControlResultsDto {
  deleteDate?: Date;
  createDate?: Date;
  checkCell?: string;
  clientTimeZone?: string;
  finalRead?: string;
  id?: number;
  initialRead?: string;
  modificationDate?: Date;
  qcEntryControlId?: number;
  qcInterpretation?: string;
}
