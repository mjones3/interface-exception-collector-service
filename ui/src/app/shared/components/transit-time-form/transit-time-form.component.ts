import { Component, effect, inject, input, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDatepicker, MatDatepickerInput, MatDatepickerToggle } from '@angular/material/datepicker';
import { MatError, MatFormField, MatHint, MatLabel, MatSuffix } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { MatOption, MatSelect } from '@angular/material/select';
import { DateTime } from 'luxon';
import { LookUpDto } from '@shared';
import { toSignal } from '@angular/core/rxjs-interop';
import { TransitTimeFormGroupValidator, TransitTimeValidationModel } from 'app/shared/forms/transit-time-form-group.validator';

import { NotificationTypeMap } from '@shared';
import { GlobalMessageComponent } from 'app/shared/components/global-message/global-message.component';
import { buildLuxonDateTimeWithParsedTimeField } from 'app/shared/utils/utils';
import { catchError, first, map, tap } from 'rxjs';
import { ApolloError } from '@apollo/client';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from 'app/shared/utils/notification.handling';
import { ToastrService } from 'ngx-toastr';
import { UseCaseNotificationDTO } from 'app/shared/models/use-case-response.dto';
import { TransitTimeService } from '@shared';

@Component({
  selector: 'biopro-transit-time-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatLabel,
    MatDatepickerToggle,
    MatHint,
    MatSuffix,
    MatInput,
    MatDatepicker,
    MatDatepickerInput,
    MatIcon,
    MatSelect,
    MatOption,
    MatError,
    GlobalMessageComponent
  ],
  templateUrl: './transit-time-form.component.html'
})
export class TransitTimeFormComponent {
  protected readonly NotificationTypeMap = NotificationTypeMap;

  formBuilder = inject(FormBuilder);
  transitTimeService = inject(TransitTimeService);
  toastrService = inject(ToastrService);

  availableTimeZones = input<LookUpDto[]>([]);
  humanReadableTime = input<string>(null);
  updateTransitTimeQuarantineSignal = output<UseCaseNotificationDTO>();
  updatetransitTimeHumanReadableSignal = output<string>()
  temperatureCategory = input<string>(null);

  now = new Date();

  formGroup = signal<FormGroup>(this.createFormGroup());

  transitTimeValueSignal = toSignal(this.formGroup().valueChanges);

  transitTimeValueChangeEffect = effect(() => {
    const transitTime = this.transitTimeValueSignal();
    if (this.formGroup().touched && this.formGroup().valid && transitTime) {
      this.triggerValidateTransitTime(transitTime).subscribe();
    } else {
      this.updateTransitTimeQuarantineSignal.emit(null);
      this.updatetransitTimeHumanReadableSignal.emit(null);
    }
  }, { allowSignalWrites: true });

  private createFormGroup(): FormGroup {
    return this.formBuilder.group({
      startDate: [null as DateTime, { updateOn: 'blur' }],
      startTime: ['', { updateOn: 'blur' }],
      startZone: ['', { updateOn: 'blur' }],
      endDate: [null as DateTime, { updateOn: 'blur' }],
      endTime: ['', { updateOn: 'blur' }],
      endZone: ['', { updateOn: 'blur' }],
    });
  }

  updateValidators(useTransitTime: boolean): void {
    if (useTransitTime) {
      this.formGroup().enable();
      this.formGroup().setValidators([TransitTimeFormGroupValidator.validator]);
      this.formGroup().controls.startDate.setValidators([Validators.required]);
      this.formGroup().controls.startTime.setValidators([Validators.required]);
      this.formGroup().controls.startZone.setValidators([Validators.required]);
      this.formGroup().controls.endDate.setValidators([Validators.required]);
      this.formGroup().controls.endTime.setValidators([Validators.required]);
      this.formGroup().controls.endZone.setValidators([Validators.required]);
    } else {
      this.formGroup().setValidators([]);
      this.formGroup().controls.startDate.setValidators([]);
      this.formGroup().controls.startTime.setValidators([]);
      this.formGroup().controls.startZone.setValidators([]);
      this.formGroup().controls.endDate.setValidators([]);
      this.formGroup().controls.endTime.setValidators([]);
      this.formGroup().controls.endZone.setValidators([]);
      this.formGroup().disable();
    }
    this.formGroup().updateValueAndValidity();
  }

  setEndZone(timeZone: string): void {
    this.formGroup().controls.endZone.setValue(timeZone);
  }

    setStartZone(timeZone: string): void {
        this.formGroup().controls.startZone.setValue(timeZone);
    }

  reset(): void {
    this.formGroup().reset();
  }

  triggerElementBlur(event: Event): void {
    const element = event.target as HTMLElement;
    element.blur();
  }

  triggerValidateTransitTime(value: TransitTimeValidationModel) {
    const startDateTime = buildLuxonDateTimeWithParsedTimeField(value.startDate, value.startTime);
    const endDateTime = buildLuxonDateTimeWithParsedTimeField(value.endDate, value.endTime);

    return this.transitTimeService
        .validateTransitTime({
            temperatureCategory: this.temperatureCategory(),
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
                this.updateTransitTimeQuarantineSignal.emit(quarantineOrNull);

                const otherNotifications = response.data?.validateTransitTime?.notifications?.filter(n => n.type !== 'CAUTION');
                consumeUseCaseNotifications(this.toastrService, otherNotifications);

                this.updatetransitTimeHumanReadableSignal.emit(response.data?.validateTransitTime?.data?.resultDescription);
            }),
            map(response => response.data?.validateTransitTime)
        );
  }
}
