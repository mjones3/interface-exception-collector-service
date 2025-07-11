import { AsyncPipe } from '@angular/common';
import { Component, computed, effect, ElementRef, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDatepickerToggle, MatDatepicker, MatDatepickerInput } from '@angular/material/datepicker';
import { MatError, MatFormField, MatHint, MatLabel, MatSuffix } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatSelect } from '@angular/material/select';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { LookUpDto, ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { DateTime } from 'luxon';
import { CookieService } from 'ngx-cookie-service';
import { Store } from '@ngrx/store';
import { Router } from '@angular/router';
import { Cookie } from 'app/shared/types/cookie.enum';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { catchError, first, map, tap } from 'rxjs';
import { UseCaseNotificationDTO } from 'app/shared/models/use-case-response.dto';
import { TransitTimeValidationModel } from 'app/shared/forms/transit-time-form-group.validator';
import { TransferInformationDTO } from '../../models/internal-transfer-order.dto';
import { ToastrService } from 'ngx-toastr';
import { find } from 'lodash';
import { TransitTimeFormComponent } from 'app/shared/components/transit-time-form/transit-time-form.component';
import { ApolloError } from '@apollo/client';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from 'app/shared/utils/notification.handling';
import { TemperatureFormComponent } from 'app/shared/components/temperature-form/temperature-form.component';
import { ReceivingService } from 'app/modules/imports/service/receiving.service';

@Component({
  selector: 'biopro-transfer-receipt',
  standalone: true,
  imports: [
    ProcessHeaderComponent,
    ActionButtonComponent,
    FuseCardComponent,
    AsyncPipe,
    TransitTimeFormComponent,
    TemperatureFormComponent,
    MatInput,
    MatFormField,
    MatLabel,
    MatError,
    ReactiveFormsModule,
    MatDatepickerToggle,
    MatHint,
    MatSuffix,
    MatIcon,
    MatSelect,
    MatDatepicker,
    MatDatepickerInput,
  ],
  templateUrl: './transfer-receipt.component.html'
})
export class TransferReceiptComponent {

  internalTransferMock = [
    {
      id: 1,
      orderNumber: '123',
      temperatureUnit: 'roomtemperature'
    },
    {
      id: 2,
      orderNumber: '234',
      temperatureUnit: 'frozen'
    },
    {
      id: 3,
      orderNumber: '345',
      temperatureUnit: 'ref'
    }
  ]

  processHeaderService = inject(ProcessHeaderService);
  formBuilder = inject(FormBuilder);
  cookieService = inject(CookieService);
  router = inject(Router);
  store = inject(Store);
  receivingService = inject(ReceivingService);
  toastrService = inject(ToastrService);


  transferInformationForm = this.formBuilder.group({
    transferOrderNumber: ['', [ Validators.required ]],
    temperatureCategory: [{value: '', disabled: true}],
    comments: ['']
  });

  locationCodeComputed = computed(() => this.cookieService.get(Cookie.XFacility));
  employeeIdComputed = toSignal(this.store.select(getAuthState).pipe(map(auth => auth['id'])));
  availableTimeZonesSignal = computed<LookUpDto[]>(() => this.internalTransferInformationSignal()?.transitTimeZoneList ?? []);

  internalTransferInformationSignal = signal<TransferInformationDTO>({} as TransferInformationDTO);
  transitTimeHumanReadableSignal = signal<string>(null);
  transitTimeQuarantineSignal = signal<UseCaseNotificationDTO>(null);
  transferOrderNumberSignal = toSignal(this.transferInformationForm.controls.transferOrderNumber.valueChanges);

  //TODO
  transferOrderNumberChangeEffect = effect(() => {
    const transferOrderNumber = this.transferOrderNumberSignal();
    const value = find(this.internalTransferMock, {orderNumber: transferOrderNumber});
    if(null != value){
      this.transferInformationForm.patchValue({temperatureCategory: value.temperatureUnit});
      let signalValue = this.internalTransferInformationSignal();
      signalValue.temperatureUnit = value.temperatureUnit;
      this.internalTransferInformationSignal.set(signalValue)
    }else{
      this.transferInformationForm.patchValue({temperatureCategory: ''});
      let signalValue = this.internalTransferInformationSignal();
      signalValue.temperatureUnit = '';
      this.internalTransferInformationSignal.set(signalValue)
    }
  }, { allowSignalWrites: true })
  
  temperatureQuarantineSignal = signal<UseCaseNotificationDTO>(null);
  thermometerField = viewChild<ElementRef<HTMLInputElement>>('thermometerId');
  temperatureField = viewChild<ElementRef<HTMLInputElement>>('temperature');
  now = new Date();
  readonly commentsMaxLength = 250;

  cancel(): void {
    this.transferInformationForm.reset();
  }

  transferContinue(){
    //TODO
  }
  
  onTransitTimeChange(transitTime: TransitTimeValidationModel): void {
    this.triggerValidateTransitTime(this.transferInformationForm.controls.temperatureCategory.value, transitTime).subscribe();
  }

  triggerValidateTransitTime(temperatureProductCategory: string, value: TransitTimeValidationModel) {
    const startDateTime = this.buildLuxonDateTimeWithParsedTimeField(value.startDate, value.startTime);
    const endDateTime = this.buildLuxonDateTimeWithParsedTimeField(value.endDate, value.endTime);

    return this.receivingService
        .validateTransitTime({
            temperatureCategory: temperatureProductCategory,
            startDateTime: startDateTime.toISO({ includeOffset: false, suppressMilliseconds: true }) + 'Z',
            startTimeZone: value.startZone,
            endDateTime: endDateTime.toISO({ includeOffset: false, suppressMilliseconds: true }) + 'Z',
            endTimeZone: value.endZone,
        })
        .pipe(
            first(),
            catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
            tap(response => {
                const quarantineOrNull = response.data?.validateTransitTime?.notifications?.filter(n => n.type === 'CAUTION')?.[0] ?? null;
                this.transitTimeQuarantineSignal.set(quarantineOrNull);

                const otherNotifications = response.data?.validateTransitTime?.notifications?.filter(n => n.type !== 'CAUTION');
                consumeUseCaseNotifications(this.toastrService, otherNotifications);

                this.transitTimeHumanReadableSignal.set(response.data?.validateTransitTime?.data?.resultDescription);
            }),
            map(response => response.data?.validateTransitTime)
        );
  }

  onTemperatureChange(data: { temperatureProductCategory: string; temperature: number }): void {
    this.triggerValidateTemperature(this.transferInformationForm.controls.temperatureCategory.value, data.temperature).subscribe();
  }

  triggerValidateTemperature(
    temperatureProductCategory: string,
    temperature: number,
  ) {
    return this.receivingService
        .validateTemperature({
            temperature,
            temperatureCategory: temperatureProductCategory
        })
        .pipe(
            first(),
            catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
            tap(response => {
                const quarantineOrNull = response.data?.validateTemperature?.notifications?.filter(n => n.type === 'CAUTION')?.[0] ?? null;
                this.temperatureQuarantineSignal.set(quarantineOrNull);

                const otherNotifications = response.data?.validateTemperature?.notifications?.filter(n => n.type !== 'CAUTION');
                consumeUseCaseNotifications(this.toastrService, otherNotifications);
            }),
            map(response => response.data?.validateTemperature)
        );
  }

  buildLuxonDateTimeWithParsedTimeField(date: DateTime, hh24mm60TimeSeparatedByColon: string): DateTime<boolean> {
    if (!date || !hh24mm60TimeSeparatedByColon) {
        return null
    }
    const [hours, minutes] = hh24mm60TimeSeparatedByColon.split(':');
    return DateTime.fromISO(date.toISODate()).set({ hour: +hours, minute: +minutes });
  }

  triggerElementBlur(event: Event): void {
    const element = event.target as HTMLElement;
    element.blur();
  }
}
