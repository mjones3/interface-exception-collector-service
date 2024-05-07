import { HttpClientTestingModule } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { getAppInitializerMockProvider } from '@rsa/commons';
import { NotificationEventService } from './notification-event.service';

describe('NotificationEventService', () => {
  let service: NotificationEventService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [...getAppInitializerMockProvider('commons-lib')],
    });
    service = TestBed.inject(NotificationEventService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
