import { ReagentsAndControlsEntryResultDto } from './reagents-and-controls-entry-result.dto';

export interface ReagentsAndControlsEntryDto {
  id?: number;
  reagentControlTypeId?: number;
  locationId?: number;
  deviceId?: number;
  reviewTypeId?: number;
  entryEmployeeId?: string;
  lockEmployeeId?: string;
  retries?: number;
  startReviewDate?: Date;
  endReviewDate?: Date;
  expirationDate?: Date;
  deleteDate?: Date;
  createDate?: Date;
  modificationDate?: Date;
  entryResults: ReagentsAndControlsEntryResultDto[];
  comments?: string;
  expirationDateFormatted?: string;
  [key: string]: any;
}
