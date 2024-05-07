export interface ReagentsAndControlsEntryResultDto {
  id?: number;
  pqcEntryId?: number;
  reagentControlId?: number;
  minValue?: number;
  maxValue?: number;
  lotNumber?: string;
  expirationDate?: Date;
  openExpirationDate?: Date;
  result?: string;
  deleteDate?: Date;
  createDate?: Date;
  modificationDate?: Date;
}
