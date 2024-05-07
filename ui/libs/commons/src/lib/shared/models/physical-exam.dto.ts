export interface PhysicalExamResultDto {
  id?: number;
  code?: string;
  text?: string;
  value?: string;
  comments?: any;
  orderNumber?: number;
  createDate?: Date;
  createDateTimezone?: string;
  modificationDate?: Date;
  modificationDateTimezone?: string;
  physicalExamId?: any;
  parentId?: number;
  childCount?: number;
}

export interface PhysicalExamDto {
  id?: number;
  donationId?: number;
  donorId?: number;
  height?: number;
  weight?: number;
  numberOfPregnancies?: number;
  comments?: string;
  employeeId?: string;
  createDate?: Date;
  createDateTimezone?: string;
  modificationDate?: Date;
  modificationDateTimezone?: string;
  results?: PhysicalExamResultDto[];
}
