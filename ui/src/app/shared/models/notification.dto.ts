export enum NotificationType {
    success = 'success',
    warning = 'WARN',
    error = 'ERROR',
    info = 'INFO',
}

export interface NotificationDto {
    name?: string;
    statusCode: number;
    notificationType: keyof typeof NotificationType;
    message: string;
    notificationEventOnDismiss?: string;
    action: string;
    reason: string;
    code: number;
}
