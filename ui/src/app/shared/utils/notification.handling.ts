import {
    NotificationDto,
    NotificationTypeMap,
    ToastrImplService,
} from '@shared';
import { ToastrService } from 'ngx-toastr';
import { Notification } from '../../modules/orders/models/notification.dto';
import { UseCaseNotificationDTO } from '../models/use-case-response.dto';

export function consumeNotification(
    toaster: ToastrService | ToastrImplService,
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
    toaster: ToastrService | ToastrImplService,
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
    toaster: ToastrService | ToastrImplService,
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
    toaster: ToastrService | ToastrImplService,
    notifications: Notification[],
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onTapFn: () => void = () => {}
): void {
    notifications?.forEach((notification) =>
        consumeNotificationMessage(toaster, notification, onTapFn)
    );
}

/*
 * Notification and NotificationDTO does not share the same structure.
 * FIXME refactor and sync notifications structures.
 */
export function consumeUseCaseNotification(
    toaster: ToastrService | ToastrImplService,
    notification: UseCaseNotificationDTO,
    onTapFn: () => void
): void {
    toaster
        .show(
            notification.message,
            null,
            {},
            NotificationTypeMap[notification.type].type
        )
        ?.onTap.subscribe(() => onTapFn());
}

/*
 * Notification and NotificationDTO does not share the same structure.
 * FIXME refactor and sync notifications structures.
 */
export function consumeUseCaseNotifications(
    toaster: ToastrService | ToastrImplService,
    notifications: UseCaseNotificationDTO[],
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    onTapFn: () => void = () => {}
): void {
    notifications?.forEach((notification) =>
        consumeUseCaseNotification(toaster, notification, onTapFn)
    );
}
