import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import { take } from 'rxjs/operators';
import { NotificationEventOnDismissType } from '../../shared/models/notification.dto';

@Injectable({
  providedIn: 'root',
})
export class NotificationEventService {
  private notificationSubject = new Subject<NotificationEventOnDismissType>();
  private notificationArraySubject = new Subject<Array<string>>();

  notification$ = this.notificationSubject.asObservable();
  notificationArray$ = this.notificationArraySubject.asObservable();

  constructor() {}

  addNotificationEvent(event: NotificationEventOnDismissType, origin$: Observable<any>) {
    origin$.pipe(take(1)).subscribe(() => this.notificationSubject.next(event));
  }

  addNotificationEventMessages(messages: Array<string>, origin$: Observable<any>) {
    origin$.pipe(take(1)).subscribe(() => this.notificationArraySubject.next(messages));
  }
}
