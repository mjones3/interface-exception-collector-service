import {
    NotificationDto,
    NotificationTypeMap,
    ToastrImplService,
} from '@shared';

export function consumeNotification(
    toaster: ToastrImplService,
    notification: NotificationDto,
    onTapFn: () => void
): void {
    toaster
        .show(
            notification.message,
            null,
            {},
            NotificationTypeMap[notification.notificationType].type
        )
        .onTap.subscribe(() => onTapFn());
}

export function consumeNotifications(
    toaster: ToastrImplService,
    notifications: NotificationDto[],
    onTapFn: () => void = () => {}
): void {
    notifications?.forEach((notification) =>
        consumeNotification(toaster, notification, onTapFn)
    );
}
