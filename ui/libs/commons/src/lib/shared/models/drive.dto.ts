export interface DriveDto {
  id: number;
  driveId: string;
  regionDesc?: string;
  regionCcfin?: string;
  locationCode?: number;
  location?: string;
  address?: string;
  state?: string;
  siteType?: string;
  sponsorCode?: number;
  sponsor?: string;
  startTime?: Date;
  endTime?: Date;
  supervisor?: string;
  employeeCode?: string;
  timezoneDesc?: string;
  createDate?: Date;
  modificationDate?: Date;
  properties?: {
    sixteenAllowed?: string;
    vehicleId?: string;
  };
}
