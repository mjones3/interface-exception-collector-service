export interface PatientDto {
  id?: number;
  firstName?: string;
  lastName?: string;
  middleInitial?: string;
  gender?: string;
  dob?: string;
  abo?: string;
  rh?: string;
  deceased?: boolean;
  createDate?: Date;
  modificationDate?: Date;
  deleteDate?: Date;
  properties?: Map<string, string>;
  type?: string;
}

export interface PractitionerDto {
  deleteDate?: Date;
  createDate?: Date;
  firstName?: string;
  id?: number;
  lastName?: string;
  middleInitial?: string;
  modificationDate?: Date;
  suffix?: string;
}
