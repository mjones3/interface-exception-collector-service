import { Component, computed, effect, ElementRef, inject, OnInit, signal, viewChild } from '@angular/core';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { AsyncPipe } from '@angular/common';
import { FuseCardComponent } from '../../../../../@fuse';
import { LookUpDto, NotificationTypeMap, ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFabButton } from '@angular/material/button';
import { MatDatepicker, MatDatepickerInput, MatDatepickerToggle } from '@angular/material/datepicker';
import { MatError, MatFormField, MatHint, MatLabel, MatSuffix } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { ReceivingService } from '../../service/receiving.service';
import { Store } from '@ngrx/store';
import { getAuthState } from '../../../../core/state/auth/auth.selectors';
import { catchError, combineLatestWith, EMPTY, first, map, Observable, switchMap, tap } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { ApolloError } from '@apollo/client';
import {
    ShippingInformationDTO,
    TemperatureProductCategoryIconMap
} from '../../graphql/query-definitions/imports-enter-shipping-information.graphql';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatOption, MatSelect } from '@angular/material/select';
import { DeviceIdValidator } from '../../validators/device-id.validator';
import { ToastrService } from 'ngx-toastr';
import { GlobalMessageComponent } from '../../../../shared/components/global-message/global-message.component';
import { UseCaseNotificationDTO } from '../../../../shared/models/use-case-response.dto';
import {
    TransitTimeFormGroupValidator,
    TransitTimeValidationModel
} from '../../validators/transit-time-form-group.validator';
import { DateTime } from 'luxon';
import { fromPromise } from 'rxjs/internal/observable/innerFrom';

@Component({
  selector: 'biopro-imports-enter-shipment-information',
  standalone: true,
    imports: [
        ActionButtonComponent,
        AsyncPipe,
        FuseCardComponent,
        ProcessHeaderComponent,
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
        MatFabButton,
        MatSelect,
        MatOption,
        MatError,
        GlobalMessageComponent
    ],
  templateUrl: './imports-enter-shipment-information.component.html',
    styleUrls: [ './imports-enter-shipment-information.component.scss' ],
})
export class ImportsEnterShipmentInformationComponent implements OnInit {

    protected readonly TemperatureProductCategoryIconMap = TemperatureProductCategoryIconMap;
    protected readonly NotificationTypeMap = NotificationTypeMap;

    router = inject(Router);
    store = inject(Store);
    formBuilder = inject(FormBuilder);
    toastrService = inject(ToastrService);
    processHeaderService = inject(ProcessHeaderService);
    receivingService = inject(ReceivingService);
    cookieService = inject(CookieService);

    thermometerField = viewChild<ElementRef<HTMLInputElement>>('thermometerId');
    temperatureField = viewChild<ElementRef<HTMLInputElement>>('temperature');

    form = this.formBuilder.group({
        temperatureProductCategory: ['', [ Validators.required ]],
        transitTime: this.formBuilder.group({
            startDate: [null as DateTime, { updateOn: 'blur' }],
            startTime: ['', { updateOn: 'blur' }],
            startZone: ['', { updateOn: 'blur' }],
            endDate: [null as DateTime, { updateOn: 'blur' }],
            endTime: ['', { updateOn: 'blur' }],
            endZone: ['', { updateOn: 'blur' }],
        }),
        temperature: this.formBuilder.group({
            thermometerId: ['', { updateOn: 'blur' }],
            temperature: [0, { updateOn: 'blur' }],
        }),
        comments: ['', []]
    });

    productCategoryLookupsSignal = signal<LookUpDto[]>(null);
    now = new Date();

    locationCodeComputed = computed(() => this.cookieService.get(Cookie.XFacility));
    employeeIdComputed = toSignal(this.store.select(getAuthState).pipe(map(auth => auth['id'])));
    shippingInformationSignal = signal<ShippingInformationDTO>(null);
    availableTimeZonesSignal = computed<LookUpDto[]>(() => this.shippingInformationSignal()?.transitTimeZoneList ?? []);

    transitTimeHumanReadableSignal = signal<string>(null);
    transitTimeValueSignal = toSignal(this.form.controls.transitTime.valueChanges); // Binding valueChanges once no status is used here
    transitTimeQuarantineSignal = signal<UseCaseNotificationDTO>(null);
    transitTimeValueChangeEffect = effect(() => {
        const transitTime = this.transitTimeValueSignal();
        if (this.form.controls.transitTime.touched && this.form.controls.transitTime.valid) {
            this.triggerValidateTransitTime(this.form.controls.temperatureProductCategory.value, transitTime as TransitTimeValidationModel).subscribe();
        } else {
            this.transitTimeQuarantineSignal.set(null);
            this.transitTimeHumanReadableSignal.set(null);
        }
    }, { allowSignalWrites: true });

    thermometerIdStatusChangeSignal = toSignal(this.form.controls.temperature.controls.thermometerId.statusChanges); // Binding statusChanges once no changed value is used here
    thermometerIdStatusChangeEffect = effect(() => {
        const status = this.thermometerIdStatusChangeSignal();
        this.form.controls.temperature.controls.temperature.reset();
        if (this.form.controls.temperature.controls.thermometerId.touched && status === 'VALID') {
            this.form.controls.temperature.controls.temperature.enable();
            this.temperatureField()?.nativeElement?.focus();
        } else {
            this.form.controls.temperature.controls.temperature.disable();
            if (this.form.controls.temperature.controls.thermometerId.touched) {
                this.thermometerField()?.nativeElement?.focus();
            }
        }
    });

    temperatureValueSignal = toSignal(this.form.controls.temperature.controls.temperature.statusChanges
        .pipe(combineLatestWith(this.form.controls.temperature.controls.temperature.valueChanges)));
    temperatureQuarantineSignal = signal<UseCaseNotificationDTO>(null);
    temperatureValueChangeEffect = effect(() => {
        const [ status, value ] = this.temperatureValueSignal();
        if (status === 'VALID' && value !== null && value !== undefined && isFinite(value)) {
            this.triggerValidateTemperature(this.form.controls.temperatureProductCategory.value, value).subscribe();
        } else if (!value) {
            this.temperatureQuarantineSignal.set(null);
        }
    }, { allowSignalWrites: true });

    ngOnInit() {
        this.form.reset();
        this.fetchLookups().subscribe();
    }

    createImport(): void {
        this.triggerCreateImport().subscribe();
    }

    cancel(): void {
        this.form.reset();
    }

    fetchLookups(): Observable<LookUpDto[]> {
        return this.receivingService
            .findAllLookupsByType('TEMPERATURE_PRODUCT_CATEGORY')
            .pipe(
                catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
                map(response => {
                    this.productCategoryLookupsSignal.set(response.data.findAllLookupsByType)
                    return response.data.findAllLookupsByType;
                })
            );
    }

    triggerQueryEnterShippingInformation(temperatureProductCategory: string): Observable<ShippingInformationDTO> {
        return this.receivingService
            .queryEnterShippingInformation({
                productCategory: temperatureProductCategory,
                employeeId: this.employeeIdComputed(),
                locationCode: this.locationCodeComputed()
            })
            .pipe(
                first(),
                catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
                tap(response => consumeUseCaseNotifications(this.toastrService, response.data?.enterShippingInformation?.notifications)),
                map((response) => {
                    const { data } = response.data.enterShippingInformation;
                    this.shippingInformationSignal.set(data);
                    return data;
                })
            );
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

    triggerCreateImport() {
        return this.receivingService
            .createImport({
                temperatureCategory: this.form.controls?.temperatureProductCategory?.value,
                transitStartDateTime: this.buildLuxonDateTimeWithParsedTimeField(this.form.controls?.transitTime?.controls?.startDate?.value, this.form.controls?.transitTime?.controls?.startTime?.value)?.toISO(),
                transitStartTimeZone: this.form.controls?.transitTime?.controls?.startZone?.value,
                transitEndDateTime: this.buildLuxonDateTimeWithParsedTimeField(this.form.controls?.transitTime?.controls?.endDate?.value, this.form.controls?.transitTime?.controls?.endTime?.value)?.toISO(),
                transitEndTimeZone: this.form.controls?.transitTime?.controls?.endZone?.value,
                temperature: this.form.controls?.temperature?.controls?.temperature?.value,
                thermometerCode: this.form.controls?.temperature?.controls?.thermometerId?.value,
                locationCode: this.locationCodeComputed(),
                comments: this.form.controls?.comments?.value,
                employeeId: this.employeeIdComputed(),
            })
            .pipe(
                first(),
                catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
                tap(response => consumeUseCaseNotifications(this.toastrService, response.data?.createImport?.notifications)),
                map(response => response.data?.createImport),
                switchMap(response =>
                    response.notifications?.[0]?.type === 'SYSTEM'
                        ? fromPromise(this.router.navigateByUrl(response._links.next))
                        : EMPTY
                )
            );
    }

    selectCategoryFromLookup(temperatureProductCategoryLookup: LookUpDto): void {
        this.selectCategory(temperatureProductCategoryLookup.optionValue);
    }

    selectCategory(temperatureProductCategory: string): void {
        this.form.reset();
        this.triggerQueryEnterShippingInformation(temperatureProductCategory)
            .subscribe(shippingInformationDTO => {
                this.updateFormValidators(shippingInformationDTO);
                this.form.controls.temperatureProductCategory.setValue(temperatureProductCategory);
                this.form.controls.transitTime.controls.endZone.setValue(shippingInformationDTO.defaultTimeZone);
                this.form.updateValueAndValidity();
            });
    }

    triggerElementBlur(event: Event): void {
        const element = event.target as HTMLElement;
        element.blur();
    }

    updateFormValidators(shippingInformationDTO: ShippingInformationDTO): void {
        this.updateFormValidationForTransitTime(shippingInformationDTO.displayTransitInformation);
        this.updateFormValidationForTemperature(shippingInformationDTO.displayTemperature);
    }

    updateFormValidationForTransitTime(useTransitTime: boolean) {
        if (useTransitTime) {
            this.form.controls.transitTime.enable();
            this.form.controls.transitTime.setValidators([ TransitTimeFormGroupValidator.validator ])
            this.form.controls.transitTime.controls.startDate.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.startTime.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.startZone.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.endDate.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.endTime.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.endZone.setValidators([ Validators.required ]);
            this.form.controls.transitTime.setAsyncValidators([]);
        } else {
            this.form.controls.transitTime.setValidators([])
            this.form.controls.transitTime.controls.startDate.setValidators([]);
            this.form.controls.transitTime.controls.startTime.setValidators([]);
            this.form.controls.transitTime.controls.startZone.setValidators([]);
            this.form.controls.transitTime.controls.endDate.setValidators([]);
            this.form.controls.transitTime.controls.endTime.setValidators([]);
            this.form.controls.transitTime.controls.endZone.setValidators([]);
            this.form.controls.transitTime.setAsyncValidators([]);
            this.form.controls.transitTime.disable();
        }
    }

    updateFormValidationForTemperature(useTemperature: boolean) {
        if (useTemperature) {
            this.form.controls.temperature.enable();
            this.form.controls.temperature.controls.thermometerId.setValidators([ Validators.required ]);
            this.form.controls.temperature.controls.thermometerId.setAsyncValidators([ DeviceIdValidator.asyncValidatorUsing(this.toastrService, this.receivingService, this.locationCodeComputed()) ]);
            this.form.controls.temperature.controls.temperature.setValidators([ Validators.required, Validators.min(-273), Validators.max(99) ]);
            this.form.controls.temperature.controls.temperature.disable(); // Temperature should start as disabled field
        } else {
            this.form.controls.temperature.controls.thermometerId.setValidators([]);
            this.form.controls.temperature.controls.thermometerId.setAsyncValidators([]);
            this.form.controls.temperature.controls.temperature.setValidators([]);
            this.form.controls.temperature.disable();
        }
    }

    buildLuxonDateTimeWithParsedTimeField(date: DateTime, hh24mm60TimeSeparatedByColon: string): DateTime<boolean> {
        if (!date || !hh24mm60TimeSeparatedByColon) {
            return null
        }
        const [hours, minutes] = hh24mm60TimeSeparatedByColon.split(':');
        return DateTime.fromISO(date.toISODate()).set({ hour: +hours, minute: +minutes });
    }

}
