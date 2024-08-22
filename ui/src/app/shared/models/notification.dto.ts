export enum NotificationType {
    success,
    warning,
    error,
}

export interface NotificationDto {
    statusCode: number;
    notificationType: keyof typeof NotificationType;
    message: string;
    notificationEventOnDismiss?: string;
}
