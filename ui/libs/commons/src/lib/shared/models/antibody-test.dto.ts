export interface AntibodyTestDto {
  id?: number;
  unitNumber: string;
  antibodyBatchId?: number;
  antibodyTestTypeId: number;
  locationId: number;
  statusKey: string;
  comments?: string;
  employeeId?: string;
  reviewEmployeeId?: string;
  reviewDate?: string;
  results?: string[];
  reviewResults?: string[];
  createDate?: string;
  modificationDate?: string;
  deleteDate?: string;
}

export interface AntibodyTestTypeDto {
  id?: number;
  descriptionKey: string;
  multipleResults: boolean;
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
}

export interface AntibodyTestTypeEntryDto {
  id?: number;
  antibodyTestTypeId: number;
  valueKey: string;
  value: string;
  orderNumber: number;
  active: boolean;
  createDate?: string;
  modificationDate?: string;
}

export interface PreviousAntibodyTestResultDto {
  unitNumber?: string;
  antibodyTestTypeId?: number;
  donorId?: number;
  testCreateDate?: string;
  result?: string;
  multipleResults?: boolean;
}

export interface AntibodyBatchDto {
  id?: number;
  locationId?: number;
  employeeId?: string;
  unitNumbers?: string[];
  createDate?: Date;
  createDateTimezone?: string;
  modificationDate?: Date;
  modificationDateTimezone?: string;
  deleteDate?: null;
  deleteDateTimezone?: null;
}
