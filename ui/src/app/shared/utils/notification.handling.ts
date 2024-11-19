import {
    NotificationDto,
    NotificationTypeMap,
    ToastrImplService,
} from '@shared';

export function consumeNotification(
    toaster: ToastrImplService,
    notification: NotificationDto,
    callBackFn
): void {
    toaster
        .show(
            notification.message,
            null,
            {},
            NotificationTypeMap[notification.notificationType].type
        )
        .onTap.subscribe(() => callBackFn());
}

export function consumeNotifications(
    toaster: ToastrImplService,
    notifications: NotificationDto[],
    callBackFn = () => {}
): void {
    notifications?.forEach((notification) =>
        consumeNotification(toaster, notification, callBackFn)
    );
}
