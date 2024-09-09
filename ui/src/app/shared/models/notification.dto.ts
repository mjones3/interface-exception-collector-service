export enum NotificationType {
    success = 'success',
    warning = 'warning',
    error = 'error',
    info = 'info',
    basic = 'basic',
}

export interface NotificationConfig {
    type: NotificationType;
    timeOut: number;
    tapToDismiss?: boolean;
}

export const NotificationTypeMap: Record<string, NotificationConfig> = {
    SYSTEM: {
        type: NotificationType.info,
        timeOut: 10000,
    },
    success: {
        type: NotificationType.success,
        timeOut: 10000,
    },
    WARN: {
        type: NotificationType.warning,
        timeOut: 10000,
    },
    ERROR: {
        type: NotificationType.error,
        timeOut: 0,
        tapToDismiss: false,
    },
};

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
