import { HgbsKitReagentsDto } from './hgbs-kits-reagents.dto';
import { WorkstationDto } from './workstation.dto';

export interface HgbsReagentsKitsListDto {
  id?: number;
  locationId?: number;
  kit?: string;
  qcInterpretation?: string;
  status?: string;
  createDate?: string;
  modificationDate?: string;
  modificationDateTimezone?: string;
  deleteDate?: Date;
  deleteDateTimezone?: string;
}

//hgbs kits list expansion row
export interface HgbsReagentsKitsListDetailsDto {
  id?: number;
  manufacturer?: string;
  expirationDate?: Date;
  openExpirationDate?: Date;
  lotNumber?: string;
  createDate?: Date;
  modificationDate?: Date;
  modificationDateTimezone?: string;
  deleteDate?: Date;
  deleteDateTimezone?: string;
}

export interface HgbsEntriesDto {
  comments?: string;
  createDate?: Date;
  createDateTimezone?: string;
  deleteDate?: Date;
  deleteDateTimezone?: string;
  downtimeEntry?: boolean;
  durationDate?: Date;
  durationDateTimezone?: string;
  entryControlResults?: HgbsEntryControlResultsDto[];
  entryDate?: Date;
  entryDateTimezone?: string;
  entryEmployeeId?: string;
  entryReagents?: HgbsEntryReagentsDto[];
  entryWorkstations?: WorkstationDto[];
  formattedDurationTime?: string;
  id?: number;
  kit?: {
    active?: boolean;
    createDate?: Date;
    hgbsKitEntries?: any;
    hgbsResultInterpretations?: any;
    id?: number;
    kitNameKey?: string;
    kitReagents?: Array<HgbsKitReagentsDto>;
    modificationDate?: Date;
    orderNumber?: number;
  };
  locationId?: number;
  modificationDate?: Date;
  modificationDateTimezone?: string;
  qcInterpretation?: string;
  reviewDate?: Date;
  reviewDateTimezone?: string;
  reviewEmployeeId?: number;
  status?: string;
}

export interface HgbsEntryControlResultsDto {
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

export interface HgbsEntryReagentsDto {
  createDate?: Date;
  createDateTimezone?: string;
  deleteDate?: Date;
  deleteDateTimezone?: null;
  expirationDate?: Date;
  expirationDateTimezone?: string;
  hgbsKitEntryId?: number;
  id?: number;
  lotNumber?: string;
  manufacturerId?: number;
  modificationDate?: Date;
  modificationDateTimezone?: string;
  openExpirationDate?: Date;
  openExpirationDateTimezone?: string;
  reagentId?: number;
}

export interface HgbsReagentsKitsListReportDto {
  kitId?: number;
  kitNameKey?: string;
  qcInterpretation?: string;
  status?: string;
  entryId?: number;

  reagents?: HgbsReagentsKitsListReportDetailsDto[];
}

export interface HgbsReagentsKitsListReportDetailsDto {
  reagentDescriptionKey?: string;
  manufacturerName?: string;
  lotNumber?: string;
  expirationDate?: Date;
  openExpirationDate?: Date;
}
