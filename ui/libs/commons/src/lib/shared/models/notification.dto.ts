import { DonorNotificationDto } from './donor-notification.dto';

export type NotificationType = 'success' | 'warning' | 'error';

export type NotificationEventOnDismissType = 'min-max-volume-eligibility';

export interface NotificationDto {
  statusCode: string;
  notificationType: NotificationType;
  message: string;
  notificationEventOnDismiss?: NotificationEventOnDismissType;
}

export interface NotificationFormOptionsDto {
  id: number;
  code: string;
  descriptionKey: string;
  orderNumber: number;
  active: boolean;
  createDate: Date;
  modificationDate: Date;
}

export interface DonorNotificationOnDemandDto {
  donors: DonorNotificationBatchDto[];
  typeKey: string;
  statusKey: string;
  creationTypeKey: string;
  locationId?: number;
  employeeId?: string;
  comments?: string;
  notificationForms?: NotificationFormOptionsDto[];
  thirdParties?: string[];
  clientTimeZone?: string;
  unitNumber?: string;
  donationId?: number;
}

export interface DonorNotificationHistoryDto extends DonorNotificationDto {
  selectRowOnClick?: boolean;
}
export interface DonorNotificationBatchDto {
  donorId: number;
  donationId?: number;
}
export interface DonorNotificationBatchDto {
  donorId: number;
  donationId?: number;
}

export interface DonorNotificationReportDto {
  id: string;
  donorId: number;
  donationId: number;
  facilityId: number;
  regionId: number;
  unitNumber: string;
  donorNotificationCriteriaId: number;
  typeKey: number;
  statusKey: string;
  creationTypeKey: string;
  employeeId: string;
  exportDate?: Date;
  exportDocumentId?: number;
  exportEmployeeId?: string;
  comments: string;
  createDate: Date;
  modificationDate: Date;
  deleteDate?: Date;
  clientTimezone: string;
  firstName: string;
  lastName: string;
  birthDate: Date;
  drawDate: Date;
  phoneNumbers?: string;
  formCodes: string;
  testResultGroupKey: string;
  regionName: string;
}
