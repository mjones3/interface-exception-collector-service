//import moment from 'moment';

import {
  AntiseraControlResultsDto,
  AntiseraEntryReagentsDto,
  AntiseraEntryReportsDto,
} from './antisera-entry-report.dto';
import { HgbsEntryControlResultsDto, HgbsEntryReagentsDto } from './hgbs-reagents-kits-list.dto';
import { WorkstationDto } from './workstation.dto';

export const QC_REPORT_FILTER_SESSION_KEY = 'QC_REPORT_FILTER_SESSION_KEY';
export const SPECIALTY_LAB_CONTROL_REPORT_QC_TYPE = 'SPECIALTY_LAB_CONTROL_REPORT_QC_TYPE';

export enum QcType {
  HGBS = 'HGBS_QC',
  ANTISERA = 'ANTISERA_QC',
}

export interface QCReportResultsDto {
  id?: string;
  kitNameKey?: string;
  reagentDescriptionKey?: string;
  locationId?: number;
  locationName?: string;
  qcType?: string;
  status?: string;
  entryDate?: string;
  month?: number;
  year?: number;
  antisera?: string;
  qcInterpretation?: string;
  workstations?: number[];
  workstationNames?: string;
  dateTimeEntered?: string;
  controlResultsAcceptable?: string;
  reagentsControlStatus?: string;
}

export interface Antisera {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
}

export interface ReagentsAndControlResults {
  reagentsAndControlsTypes: string;
  dateAndTimeOfTesting: string;
  testedBy: string;
  controlResultsAcceptable: string;
  reagentsAndControlsStatus: string;
  location: string;
  workstationId: string;
  reviewedDateAndTime?: string;
  reviewedBy?: string;
  reviewResult?: string;
}
export interface SickledexKit {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  openExpirationDate: string;
}
export interface NegativeControl {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  controlCellNumber: string;
}

export interface SickledexSolubilityBuffer {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  openExpirationDate: string;
}

export interface SickledexSolubilityReagentPowder {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  openExpirationDate: string;
}

export interface SickleChexPositiveControl {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  openExpirationDate: string;
}
export interface PositiveControl {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  controlCellNumber: string;
}

export interface SickleChexNegativeControl {
  manufacturer: string;
  lotNumber: string;
  expirationDate: string;
  openExpirationDate: string;
}

export interface ControlResult {
  negativeControl: string;
  positiveControl: string;
}
export interface NegativeControlResult {
  intialRead: string;
  finalRead: string;
  checkCell: string;
}
export interface PositiveControlResult {
  intialRead: string;
  finalRead: string;
}
export interface QCReportResultsDetailsDto {
  id?: number;
  locationId?: number;
  entryEmployeeId?: string;
  downTimeEntry?: boolean;
  reviewEmployeeId?: number;
  reviewDate?: Date;
  status?: string;
  comments?: string;
  entryDate?: Date;
  qcInterpretation?: string;
  durationDate?: Date;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
  reagentsAndControlResults?: ReagentsAndControlResults;
  kit?: {};
  entryWorkstations?: WorkstationDto[];
  qcType?: string;
  manufacturerId?: number;
  entryControls?: AntiseraControlResultsDto[];
  entryControlResults?: HgbsEntryControlResultsDto[] | AntiseraEntryReportsDto[];
  entryReagents?: HgbsEntryReagentsDto[] | AntiseraEntryReagentsDto[];
  reagent?: {
    active?: boolean;
    createDate?: Date;
    descriptionKey?: string;
    htmlTag?: any;
    id?: number;
    modificationDate?: Date;
    orderNumber?: number;
    qcType?: string;
  };
}

export interface SelectOptions {
  id: number;
  descriptionKey: string;
}

export interface AppliedFilters {
  //date: moment.Moment;
  beginEntryDate?: number;
  endEntryDate?: number;
  entryDate?: number;
  filterType: string;
  location?: SelectOptions[];
  reagentsControlStatus?: SelectOptions[];
  reagentsControlTypes?: SelectOptions;
  workstationID?: SelectOptions[];
  antisera?: SelectOptions[];
}

export enum ReviewResultOption {
  SATISFACTORY = 'Satisfactory',
  UNSATISFACTORY = 'Unsatisfactory',
}

export interface DialogResult {
  reviewResult: ReviewResultOption;
  comment: string;
}

export enum RegantControlStatus {
  PENDING_REVIEW = 'Pending Review',
}
