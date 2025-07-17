import { Injectable } from '@angular/core';
import { NotificationDto } from '@shared';

@Injectable({
    providedIn: 'root',
})
export class NotificationCriteriaService {

    filterOutByCriteria(
        notifications: NotificationDto[],
        criteria: Partial<Pick<NotificationDto, 'notificationType' | 'name' | 'action'>>
    ): NotificationDto[] {
        // Filtering notifications according to sample
        const filteredNotifications = notifications
            .filter((n) =>
                (criteria?.notificationType ? n.notificationType === criteria?.notificationType : true) &&
                (criteria?.name ? n.name === criteria?.name : true) &&
                (criteria?.action ? n.action === criteria?.action : true)
            );

        // Removing filtered notifications from original array
        for (const notification of filteredNotifications) {
            const i = notifications
                .findIndex((n) =>
                    n.notificationType === notification.notificationType &&
                    n.name === notification.name &&
                    n.action === notification.action
                );
            notifications.splice(i, 1);
        }
        return filteredNotifications;
    }

}
