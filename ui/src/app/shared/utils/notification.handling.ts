import {
    NotificationDto,
    NotificationTypeMap,
    ToastrImplService,
} from '@shared';
import { Notification } from '../../modules/orders/models/notification.dto';

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
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onTapFn: () => void = () => {}
): void {
    notifications?.forEach((notification) =>
        consumeNotification(toaster, notification, onTapFn)
    );
}

/*
 * Notification and NotificationDTO does not share the same structure.
 * FIXME refactor and sync notifications structures.
 */
export function consumeNotificationMessage(
    toaster: ToastrImplService,
    notification: Notification,
    onTapFn: () => void
): void {
    toaster
        .show(
            notification.notificationMessage,
            null,
            {},
            NotificationTypeMap[notification.notificationType].type
        )
        .onTap.subscribe(() => onTapFn());
}

/*
 * Notification and NotificationDTO does not share the same structure.
 * FIXME refactor and sync notifications structures.
 */
export function consumeNotificationMessages(
    toaster: ToastrImplService,
    notifications: Notification[],
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onTapFn: () => void = () => {}
): void {
    notifications?.forEach((notification) =>
        consumeNotificationMessage(toaster, notification, onTapFn)
    );
}
