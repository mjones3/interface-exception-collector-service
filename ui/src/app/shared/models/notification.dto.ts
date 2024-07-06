

export type NotificationType = 'success' | 'warning' | 'error';

export type NotificationEventOnDismissType = 'min-max-volume-eligibility';

export interface NotificationDto {
    statusCode: string;
    notificationType: NotificationType;
    message: string;
    notificationEventOnDismiss?: NotificationEventOnDismissType;
  }