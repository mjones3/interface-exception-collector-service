import { HgbsKitDto } from './hgbs-kits.dto';
import { QcReagentDTO } from './qc-reagent';

export const MANUFACTURING_ID = '3638168f-f78e-4541-8055-af8fdb6f9623';
export const QC_ACTIVE = 'qc-active.label';

export interface HgbsKitEntryDTO {
  id?: number;
  kit: HgbsKitDto;
  locationId: number;
  downtimeEntry: boolean;
  status?: string;
  comments?: string;
  entryDate?: string;
  durationDate?: string;
  qcInterpretation?: string;
  entryReagents?: HgbsKitEntryReagentDTO[];
  entryControlResults?: HgbsKitEntryControlResultDTO[];
  entryWorkstations?: HgbsKitEntryWorkStationDTO[];
  formattedDurationTime?: string;
}

export interface HgbsKitEntryReagentDTO {
  id?: number;
  reagent?: QcReagentDTO;
  hgbsKitEntryId?: number;
  manufacturerId: number;
  lotNumber: string;
  expirationDate: string;
  openExpirationDate: string;
}

export interface HgbsKitEntryControlResultDTO {
  id?: number;
  hgbsKitEntryId?: number;
  controlType: string;
  read: string;
  qcInterpretation?: string;
}

export interface HgbsKitEntryWorkStationDTO {
  id?: number;
  hgbsKitEntryId?: number;
  workstationId: number;
}

export interface HgbsResultDTO {
  entryResult: string;
  hgbsResultKey: string;
}
