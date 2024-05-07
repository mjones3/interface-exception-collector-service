export interface DonorNameDto {
  id?: number;
  donorId: number;
  lastName?: string;
  firstName?: string;
  middleName?: string;
  nickName?: string;
  suffix?: string;
  createDate?: Date;
  deleteDate?: Date;
}

export interface NotificationReportDto {
  typeKey?: string;
  statusKey?: string;
  donorNotificationIds?: number[];
  formCodes?: string[];
  creationTypeKey?: string[];
  regionName?: string[];
  firstName?: string;
  lastName?: string;
  unitNumber?: string[];
  testResultGroupKey?: string[];
  clientTimezone?: string;
}

export interface PendingNotificationDto {
  donationId?: number;
  testResultGroupKey?: string;
  statusKey?: string;
  id: number;
  firstName: string;
  lastName: string;
  birthDate: Date;
  donorId: number;
  unitNumber?: string;
  notificationType?: string;
  comments?: string;
  region?: string;
  testResultsGrouping?: string;
  testsResults?: Array<TestResultsDto>;
  thirdParties?: Array<ThirdPartyDto>;
  notificationForms?: Array<NotificationFormDto>;
  selectRowOnClick?: boolean;
}

export interface NotificationFormDto {
  id?: number;
  formKey: string;
  code: string;
  descriptionKey?: string;
  orderNumber?: number;
  active?: false;
  createDate?: Date;
  modificationDate?: Date;
}

export interface ThirdPartyDto {
  thirdPartyKey: string;
}

export interface TestResultsDto {
  type?: string;
  result?: string;
}

export function sortDonorNameByDate(date1: Date, date2: Date, direction: 'ASC' | 'DESC'): number {
  return direction === 'DESC' ? date2.getTime() - date1.getTime() : date1.getTime() - date2.getTime();
}

export function getCreationDate(name: DonorNameDto): Date {
  return typeof name.createDate === 'string' ? new Date(name.createDate) : name.createDate;
}

export function sortDonorNameByCreateDateAsc(name1: DonorNameDto, name2: DonorNameDto): number {
  const createDateName1 = getCreationDate(name1);
  const createDateName2 = getCreationDate(name2);
  return sortDonorNameByDate(createDateName1, createDateName2, 'ASC');
}

export function sortDonorNameByCreateDateDesc(name1: DonorNameDto, name2: DonorNameDto): number {
  const createDateName1 = getCreationDate(name1);
  const createDateName2 = getCreationDate(name2);
  return sortDonorNameByDate(createDateName1, createDateName2, 'DESC');
}
