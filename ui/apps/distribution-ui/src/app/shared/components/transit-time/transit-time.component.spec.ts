import { DecimalPipe } from '@angular/common';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { CUSTOM_ELEMENTS_SCHEMA } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatMomentDateModule } from '@angular/material-moment-adapter';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { RouterTestingModule } from '@angular/router/testing';
import { TranslateFakeLoader, TranslateLoader, TranslateModule } from '@ngx-translate/core';
import {
  AutoFocusIfDirective,
  ControlErrorComponent,
  ControlErrorsDirective, getAppInitializerMockProvider,
  ShipmentService,
  toasterMockProvider,
  ValidationPipe,
} from '@rsa/commons';
import { addRsaIconsMock } from '@rsa/distribution/data/mock/icons.mock';
import { TransitTimeComponent } from '@rsa/distribution/shared/components/transit-time/transit-time.component';
import { createTestContext } from '@rsa/testing';
import { ToastrService } from 'ngx-toastr';
import { throwError } from 'rxjs';

describe('TransitTimeComponent', () => {
  let component: TransitTimeComponent;
  let shipmentService: ShipmentService;
  let fixture: ComponentFixture<TransitTimeComponent>;
  let toaster: ToastrService;
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [
        TransitTimeComponent,
        ControlErrorComponent,
        ControlErrorsDirective,
        AutoFocusIfDirective,
        ValidationPipe,
      ],
      imports: [
        MatDatepickerModule,
        ReactiveFormsModule,
        MatMomentDateModule,
        MatSelectModule,
        BrowserAnimationsModule,
        RouterTestingModule,
        HttpClientTestingModule,
        FormsModule,
        MatInputModule,
        MatFormFieldModule,
        TranslateModule.forRoot({
          loader: {
            provide: TranslateLoader,
            useClass: TranslateFakeLoader,
          },
        }),
      ],
      providers: [
        ...getAppInitializerMockProvider('distribution-app'),
        ...toasterMockProvider,
        ValidationPipe,
        DecimalPipe
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    });
  });
  beforeEach(() => {
    const testContext = createTestContext<TransitTimeComponent>(TransitTimeComponent);
    fixture = testContext.fixture;
    component = testContext.component;
    shipmentService = TestBed.inject(ShipmentService);
    toaster = TestBed.inject(ToastrService);
    addRsaIconsMock(testContext);
    fixture.detectChanges(true);
  });

  it('should create the transit time component', () => {
    expect(component).toBeTruthy();
  });

  it('should display the correct time', () => {
    spyOn(shipmentService, 'calculateTransiteTime').and.returnValue(
      throwError({
        error: {
          type: 'https://arc-one.com/problem/problem-with-message',
          title: 'start-date-time',
          status: 400,
          path: '/v1/transit-time/calculate',
          message: 'error.http.400',
        },
      })
    );
    spyOn(shipmentService, 'currentTime').and.returnValue({
      currentDateTime: '2022-05-11T16:26:49',
      currentDate: '2022-05-11',
      currentTime: '16:26:49',
      hours: '16',
      minutes: '26',
      zoneId: 'America/Chicago',
      isDaylight: null,
    });
    component.transitTimeGroup.patchValue({
      transitStartDate: '2022-01-01',
      transitStartTimeHours: '01',
      transitStartTimeMinutes: '01',
      transitStartTimezone: 'America/Chicago',
      transitEndDate: '2022-01-01',
      transitEndTimeHours: '01',
      transitEndTimeMinutes: '02',
      transitEndTimezone: 'America/Chicago',
    });
    component.transitTimeGroup.updateValueAndValidity();
    setTimeout(() => {
      expect(component.transitTimeGroup.get('transitStartTimezone').hasError('invalid')).toEqual(true);
      expect(component.transitTime).toBe('00 hours and 00 minutes');
    }, 300);
  });
});
