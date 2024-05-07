import { QcReagentDTO } from './qc-reagent';

export interface ReagentDTO {
  orderNumber: number;
  active: boolean;
  createDate: string;
  modificationDate: string;
  id: number;
  descriptionKey: string;
  qcType: string;
  htmlTag: string;
}

export interface QcEntryDTO {
  id?: number;
  qcType: string;
  reagent?: QcReagentDTO;
  manufacturerId: number;
  locationId: number;
  entryEmployeeId?: string;
  downtimeEntry?: boolean;
  lotNumber: string;
  expirationDate: string;
  reviewEmployeeId?: string;
  reviewDate?: Date;
  status?: string;
  comments?: string;
  entryDate?: string;
  durationDate?: Date;
  qcInterpretation?: string;
  entryWorkstations?: Array<QcEntryWorkstationDTO>;
  entryControls?: Array<QcEntryControlDTO>;
  clientTimeZone?: string;
  formattedDurationTime?: string;
}

export interface QcEntryWorkstationDTO {
  id?: number;
  qcEntryId?: number;
  workstationId: number;
}

export interface QcEntryControlDTO {
  id?: number;
  qcEntryId?: number;
  manufacturerId: number;
  qcControlType: string;
  lotNumber: string;
  expirationDate: string;
  cellNumber: string;
  entryControlResults?: Array<QcEntryControlResultsDTO>;
  clientTimeZone?: string;
}

export interface QcEntryControlResultsDTO {
  id?: number;
  qcEntryControlId?: number;
  initialRead?: string;
  finalRead?: string;
  checkCell?: string;
  qcInterpretation?: string;
  clientTimeZone?: string;
}
