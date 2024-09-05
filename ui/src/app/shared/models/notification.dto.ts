export enum NotificationType {
    success = 'success',
    warning = 'warning',
    error = 'error',
    info = 'info',
}

export const NotificationTypeMap = new Map<string, NotificationType>([
    ['success', NotificationType.success],
    ['WARN', NotificationType.warning],
    ['ERROR', NotificationType.error],
    ['INFO', NotificationType.info],
]);

export interface NotificationDto {
    name?: string;
    statusCode: number;
    notificationType: string;
    message: string;
    notificationEventOnDismiss?: string;
    action?: string;
    reason?: string;
    code: number;
}
