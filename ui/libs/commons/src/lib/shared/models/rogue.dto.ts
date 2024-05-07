export interface RogueDto {
  id?: number;
  unitNumber: string;
  donationId?: number;
  donationDate?: Date;
  motivationId?: number;
  donationTypeId?: number;
  drawDate?: Date;
  withdrawlDate?: Date;
  machineId?: number;
  bagTypeId?: string;
  resolvedDate?: Date;
  resolvedEmployeeId?: number;
  resolvedReasonKey?: string;
  deleteDate?: Date;
  createDate?: Date;
  checkInLocationId?: number;
  status?: string;
  collectionInfoReceived?: boolean;
}

export interface RogueUnitReportDto {
  rogueUnitId?: number;
  unitNumber?: string;
  checkedInLocation?: string;
  regionId?: number;
  collectionLocation?: string;
  descriptionKey?: string;
  dateOfDiscovery?: string;
  status?: string;
}

export interface RogueUpdateStatusDto {
  rogueUnitId: number;
  status: string;
  discard?: string;
  comments?: string;
  resolveReasonKey?: string;
}
