import { Component, computed, inject, OnInit, signal, viewChild } from '@angular/core';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { AsyncPipe } from '@angular/common';
import { FuseCardComponent } from '../../../../../@fuse';
import { LookUpDto, NotificationTypeMap, ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFabButton } from '@angular/material/button';
import { MatError, MatFormField, MatHint, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { ReceivingService } from '../../service/receiving.service';
import { Store } from '@ngrx/store';
import { getAuthState } from '../../../../core/state/auth/auth.selectors';
import { catchError, EMPTY, first, map, Observable, switchMap, tap } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { ApolloError } from '@apollo/client';
import {
    ShippingInformationDTO
} from '../../graphql/query-definitions/imports-enter-shipping-information.graphql';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatOption, MatSelect } from '@angular/material/select';
import { ToastrService } from 'ngx-toastr';
import { UseCaseNotificationDTO } from '../../../../shared/models/use-case-response.dto';

import { DateTime } from 'luxon';
import { fromPromise } from 'rxjs/internal/observable/innerFrom';
import { TemperatureProductCategoryIconMap } from '../../models/product-information.dto';
import { TransitTimeValidationModel } from 'app/shared/forms/transit-time-form-group.validator';
import { TemperatureFormComponent } from 'app/shared/components/temperature-form/temperature-form.component';
import { TransitTimeFormComponent } from 'app/shared/components/transit-time-form/transit-time-form.component';
import { DeviceIdValidator } from 'app/shared/forms/device-id.validator';

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
        MatHint,
        MatInput,
        MatIcon,
        MatFabButton,
        MatSelect,
        MatOption,
        MatError,
        TransitTimeFormComponent,
        TemperatureFormComponent
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

    transitTimeFormComponent = viewChild<TransitTimeFormComponent>('transitTimeForm');
    temperatureFormComponent = viewChild<TemperatureFormComponent>('temperatureForm');

    form = this.formBuilder.group({
        temperatureProductCategory: ['', [ Validators.required ]],
        comments: ['', []]
    });

    productCategoryLookupsSignal = signal<LookUpDto[]>(null);
    now = new Date();

    locationCodeComputed = computed(() => this.cookieService.get(Cookie.XFacility));
    employeeIdComputed = toSignal(this.store.select(getAuthState).pipe(map(auth => auth['id'])));
    shippingInformationSignal = signal<ShippingInformationDTO>(null);
    availableTimeZonesSignal = computed<LookUpDto[]>(() => this.shippingInformationSignal()?.transitTimeZoneList ?? []);

    transitTimeHumanReadableSignal = signal<string>(null);
    transitTimeQuarantineSignal = signal<UseCaseNotificationDTO>(null);
    temperatureQuarantineSignal = signal<UseCaseNotificationDTO>(null);

    ngOnInit() {
        this.form.reset();
        this.fetchLookups().subscribe();
    }

    createImport(): void {
        this.triggerCreateImport().subscribe();
    }

    cancel(): void {
        this.form.reset();
        this.transitTimeFormComponent()?.reset();
        this.temperatureFormComponent()?.reset();
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
        const transitTimeForm = this.transitTimeFormComponent()?.formGroup();
        const temperatureForm = this.temperatureFormComponent()?.formGroup();
        
        return this.receivingService
            .createImport({
                temperatureCategory: this.form.controls?.temperatureProductCategory?.value,
                transitStartDateTime: this.buildLuxonDateTimeWithParsedTimeField(transitTimeForm?.controls?.startDate?.value, transitTimeForm?.controls?.startTime?.value)?.toISO(),
                transitStartTimeZone: transitTimeForm?.controls?.startZone?.value,
                transitEndDateTime: this.buildLuxonDateTimeWithParsedTimeField(transitTimeForm?.controls?.endDate?.value, transitTimeForm?.controls?.endTime?.value)?.toISO(),
                transitEndTimeZone: transitTimeForm?.controls?.endZone?.value,
                temperature: temperatureForm?.controls?.temperature?.value,
                thermometerCode: temperatureForm?.controls?.thermometerId?.value,
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
                    response.notifications?.[0]?.type === 'SUCCESS'
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
        this.transitTimeFormComponent()?.reset();
        this.temperatureFormComponent()?.reset();
        this.triggerQueryEnterShippingInformation(temperatureProductCategory)
            .subscribe(shippingInformationDTO => {
                this.updateFormValidators(shippingInformationDTO);
                this.form.controls.temperatureProductCategory.setValue(temperatureProductCategory);
                this.transitTimeFormComponent()?.setEndZone(shippingInformationDTO.defaultTimeZone);
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
        this.transitTimeFormComponent()?.updateValidators(useTransitTime);
    }

    updateFormValidationForTemperature(useTemperature: boolean) {
        const deviceIdValidator = DeviceIdValidator.asyncValidatorUsing(this.toastrService, this.receivingService, this.locationCodeComputed());
        setTimeout(() => {
            this.temperatureFormComponent()?.updateValidators(useTemperature, deviceIdValidator);
        });
    }

    onTransitTimeChange(transitTime: TransitTimeValidationModel): void {
        this.triggerValidateTransitTime(this.form.controls.temperatureProductCategory.value, transitTime).subscribe();
    }

    onTemperatureChange(data: { temperatureProductCategory: string; temperature: number }): void {
        this.triggerValidateTemperature(this.form.controls.temperatureProductCategory.value, data.temperature).subscribe();
    }

    isFormValid(): boolean {
        const internalTransferFormValid = this.form.valid;
        const transitTimeValid = !this.shippingInformationSignal()?.displayTransitInformation || this.transitTimeFormComponent()?.formGroup().valid;
        const temperatureValid = !this.shippingInformationSignal()?.displayTemperature || this.temperatureFormComponent()?.formGroup().valid;
        return internalTransferFormValid && transitTimeValid && temperatureValid;
    }

    buildLuxonDateTimeWithParsedTimeField(date: DateTime, hh24mm60TimeSeparatedByColon: string): DateTime<boolean> {
        if (!date || !hh24mm60TimeSeparatedByColon) {
            return null
        }
        const [hours, minutes] = hh24mm60TimeSeparatedByColon.split(':');
        return DateTime.fromISO(date.toISODate()).set({ hour: +hours, minute: +minutes });
    }
}
