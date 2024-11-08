import { NotificationDto, NotificationTypeMap } from '@shared';
import { ToastrService } from 'ngx-toastr';

export function consumeNotification(
    toaster: ToastrService,
    notification: NotificationDto
): void {
    toaster.show(
        notification.message,
        null,
        {},
        NotificationTypeMap[notification.notificationType].type
    );
}

export function consumeNotifications(
    toaster: ToastrService,
    notifications: NotificationDto[]
): void {
    notifications?.forEach((notification) =>
        consumeNotification(toaster, notification)
    );
}
