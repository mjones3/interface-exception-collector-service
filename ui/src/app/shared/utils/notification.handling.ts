import { NotificationDto, NotificationTypeMap } from '@shared';
import { ToastrService } from 'ngx-toastr';

export function consumeNotification(
    toaster: ToastrService,
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
    toaster: ToastrService,
    notifications: NotificationDto[],
    callBackFn = () => {}
): void {
    notifications?.forEach((notification) =>
        consumeNotification(toaster, notification, callBackFn)
    );
}
