import { DecimalPipe } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { ErrorStateMatcher } from '@angular/material/core';
import { TranslateService } from '@ngx-translate/core';
import {
  DefaultErrorStateMatcher,
  FacilityService,
  LookUpDto,
  ShipmentService,
  TransitTimeRequestDto,
  ValidationPipe,
  ValidationType,
} from '@rsa/commons';
import { QUARANTINE_CONSEQUENCE_TYPE } from '@rsa/distribution/core/models/returns.models';
import * as moment from 'moment';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

@Component({
  selector: 'rsa-transit-time',
  templateUrl: './transit-time.component.html',
  providers: [{ provide: ErrorStateMatcher, useClass: DefaultErrorStateMatcher }],
})
export class TransitTimeComponent implements OnInit {
  readonly validationType = ValidationType;

  @Input() labelingProductCategoryValue: string;
  @Input() timezones: LookUpDto[] = [];

  @Output() transitTimeChanged = new EventEmitter<{
    transitTimeRequest: TransitTimeRequestDto;
    transitTime: string;
    transitResponseStatusKey: string;
    transitTimeColor: string;
  }>();

  transitTimeGroup: FormGroup;
  transitTime: string;
  transitTimeColor: string;

  constructor(
    protected fb: FormBuilder,
    private shipmentService: ShipmentService,
    private facilityService: FacilityService,
    private decimalPipe: DecimalPipe,
    private translateService: TranslateService,
    private validationPipe: ValidationPipe,
    private toaster: ToastrService
  ) {
    this.transitTimeGroup = fb.group({
      transitStartDate: ['', Validators.required, this.dateRangeAsyncValidator()],
      transitStartTimeHours: [
        '',
        [Validators.required, Validators.max(23), Validators.minLength(2)],
        this.dateRangeAsyncValidator(),
      ],
      transitStartTimeMinutes: [
        '',
        [Validators.required, Validators.max(59), Validators.minLength(2)],
        this.dateRangeAsyncValidator(),
      ],
      transitStartTimezone: ['', Validators.required, this.dateRangeAsyncValidator()],
      transitEndDate: ['', Validators.required, this.dateRangeAsyncValidator()],
      transitEndTimeHours: [
        '',
        [Validators.required, Validators.max(23), Validators.minLength(2)],
        this.dateRangeAsyncValidator(),
      ],
      transitEndTimeMinutes: [
        '',
        [Validators.required, Validators.max(59), Validators.minLength(2)],
        this.dateRangeAsyncValidator(),
      ],
      transitEndTimezone: ['', Validators.required, this.dateRangeAsyncValidator()],
    });
  }

  ngOnInit(): void {
    const currentLocationTimezone = this.facilityService.getFacilityProperty('TZ') ?? '';
    let currentLocationTimezoneOptionValue = '';

    if (currentLocationTimezone) {
      //To avoid that a value that doesn't exist on the timezones list be used as selected value on the form
      currentLocationTimezoneOptionValue =
        this.timezones?.find(timezone => timezone.optionValue === currentLocationTimezone)?.optionValue ?? '';
    }
    this.transitTimeGroup.patchValue({
      transitStartTimezone: currentLocationTimezoneOptionValue,
      transitEndTimezone: currentLocationTimezoneOptionValue,
    });

    this.shipmentService.currentTime(currentLocationTimezoneOptionValue).subscribe(currentTimeRes => {
      this.transitTimeGroup.patchValue({
        transitEndDate: currentTimeRes?.currentDate,
        transitEndTimeHours: currentTimeRes?.hours,
        transitEndTimeMinutes: currentTimeRes?.minutes,
      });
    });
  }

  calculateTime() {
    if (this.transitTimeGroup.valid) {
      this.shipmentService.calculateTransiteTime(this.transitTimeRequest).subscribe(
        transitTimeRes => {
          const times = transitTimeRes?.totalTransitTime?.split(':') ?? ['00', '00'];
          this.transitTime = `${times[0]} ${this.translateService
            .instant('hours.label')
            ?.toLowerCase()} ${this.translateService.instant('and.label')} ${times[1]} ${this.translateService
            .instant('minutes.label')
            ?.toLowerCase()}`;
          this.transitTimeColor = transitTimeRes.responseStatusType === QUARANTINE_CONSEQUENCE_TYPE ? 'red' : 'green';
          this.transitTimeChanged.emit({
            transitTimeRequest: this.transitTimeRequest,
            transitTime: this.transitTime,
            transitResponseStatusKey: transitTimeRes.responseStatusKey,
            transitTimeColor: this.transitTimeColor,
          });
        },
        err => {
          this.toaster.error('something-went-wrong.label');
          throw err;
        }
      );
    }
  }

  resetTransitionTime() {
    this.transitTime = null;
    this.transitTimeColor = null;
    this.transitTimeChanged.emit(null);
  }

  clearFormErrors() {
    Object.keys(this.transitTimeGroup.controls).forEach(key => {
      const control = this.transitTimeGroup.controls[key];
      control.setErrors(null);
    });
  }

  formControlHasInvalidDate(formControlName: string) {
    const formControl = this.transitTimeGroup.get(formControlName);
    return (
      formControl.hasError('matDatepickerParse') || formControl.hasError('invalid') || formControl.hasError('maxLength')
    );
  }

  handleUndefinedDateErrors() {
    this.markStartDateAsInvalid();
    this.markEndDateAsInvalid();
    return { invalid: true };
  }

  handleStartDateError(controlName: string) {
    this.markStartDateAsInvalid();
    return controlName.startsWith('transitStart') ? { invalid: true } : null;
  }

  handleEndDateError(controlName: string) {
    this.markEndDateAsInvalid();
    return controlName.startsWith('transitEnd') ? { invalid: true } : null;
  }

  markStartDateAsInvalid() {
    this.markControlAsInvalid('transitStartDate');
    this.markControlAsInvalid('transitStartTimeHours');
    this.markControlAsInvalid('transitStartTimeMinutes');
    this.markControlAsInvalid('transitStartTimezone');
  }

  markEndDateAsInvalid() {
    this.markControlAsInvalid('transitEndDate');
    this.markControlAsInvalid('transitEndTimeHours');
    this.markControlAsInvalid('transitEndTimeMinutes');
    this.markControlAsInvalid('transitEndTimezone');
  }

  markControlAsInvalid(formControlName: string) {
    this.transitTimeGroup.get(formControlName).setErrors({ invalid: true });
    this.transitTimeGroup.get(formControlName).markAsTouched();
  }

  getControlName(c: AbstractControl): string | null {
    const formGroup = c.parent.controls;
    return Object.keys(formGroup).find(name => c === formGroup[name]) || null;
  }

  get startTransitTimeErrorMessage() {
    const validationType = this.formControlHasInvalidDate('transitStartDate')
      ? this.validationType.INVALID
      : this.validationType.REQUIRED;
    return this.validationPipe.transform('transit-start-date.label', validationType);
  }

  get endTransitTimeErrorMessage() {
    const valitationType = this.formControlHasInvalidDate('transitEndDate')
      ? this.validationType.INVALID
      : this.validationType.REQUIRED;
    return this.validationPipe.transform('transit-end-date.label', valitationType);
  }

  get transitTimeRequest() {
    return <TransitTimeRequestDto>{
      productCategory: this.labelingProductCategoryValue,
      transitStartDate: moment(this.transitTimeGroup?.get('transitStartDate')?.value).format('YYYY-MM-DD'),
      transitStartTime: `${this.decimalPipe.transform(
        this.transitTimeGroup?.get('transitStartTimeHours')?.value,
        '2.0-0'
      )}:${this.decimalPipe.transform(this.transitTimeGroup?.get('transitStartTimeMinutes')?.value, '2.0-0')}:00`,
      transitStartTimeZone: this.transitTimeGroup?.get('transitStartTimezone')?.value,
      transitEndDate: moment(this.transitTimeGroup?.get('transitEndDate')?.value).format('YYYY-MM-DD'),
      transitEndTime: `${this.decimalPipe.transform(
        this.transitTimeGroup?.get('transitEndTimeHours')?.value,
        '2.0-0'
      )}:${this.decimalPipe.transform(this.transitTimeGroup?.get('transitEndTimeMinutes')?.value, '2.0-0')}:00`,
      transitEndTimeZone: this.transitTimeGroup?.get('transitEndTimezone')?.value,
    };
  }

  get allFieldsHaveValue() {
    return (
      this.transitTimeGroup?.get('transitStartDate')?.value &&
      this.transitTimeGroup?.get('transitStartTimeHours')?.value &&
      this.transitTimeGroup?.get('transitStartTimeMinutes')?.value &&
      this.transitTimeGroup?.get('transitStartTimezone')?.value &&
      this.transitTimeGroup?.get('transitEndDate')?.value &&
      this.transitTimeGroup?.get('transitEndTimeHours')?.value &&
      this.transitTimeGroup?.get('transitEndTimeMinutes')?.value &&
      this.transitTimeGroup?.get('transitEndTimezone')?.value &&
      !this.transitTimeGroup?.get('transitStartDate')?.hasError('matDatepickerParse') &&
      !this.transitTimeGroup?.get('transitStartTimeHours')?.hasError('max') &&
      !this.transitTimeGroup?.get('transitStartTimeMinutes')?.hasError('max') &&
      !this.transitTimeGroup?.get('transitEndDate')?.hasError('matDatepickerParse') &&
      !this.transitTimeGroup?.get('transitEndTimeHours')?.hasError('max') &&
      !this.transitTimeGroup?.get('transitEndTimeMinutes')?.hasError('max')
    );
  }

  private dateRangeAsyncValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors> => {
      if (!this.allFieldsHaveValue) {
        return of(null);
      }

      const controlName = this.getControlName(control);
      return this.shipmentService.calculateTransiteTime(this.transitTimeRequest).pipe(
        map(res => {
          return { invalid: false, error: null };
        }),
        catchError(err => {
          return of({ invalid: true, error: err.error.title });
        }),
        map(res => {
          this.clearFormErrors();
          if (res.error === 'start-date-time') return this.handleStartDateError(controlName);
          if (res.error === 'end-date-time') return this.handleEndDateError(controlName);
          if (res.invalid) return this.handleUndefinedDateErrors();
          return null;
        })
      );
    };
  }
}
