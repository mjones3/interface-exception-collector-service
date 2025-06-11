import { Component, computed, effect, inject, OnInit, Renderer2, signal } from '@angular/core';
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
import { catchError, combineLatestWith, debounceTime, map, Observable, tap } from 'rxjs';
import { CookieService } from 'ngx-cookie-service';
import { ApolloError } from '@apollo/client';
import {
    ShippingInformationDTO,
    TemperatureProductCategory,
    TemperatureProductCategoryIconMap
} from '../../graphql/query-definitions/imports-enter-shipping-information.graphql';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from '../../../../shared/utils/notification.handling';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatOption, MatSelect } from '@angular/material/select';
import { DeviceIdValidator } from '../../validators/deviceIdValidator';
import { ToastrService } from 'ngx-toastr';
import { GlobalMessageComponent } from '../../../../shared/components/global-message/global-message.component';
import { UseCaseNotificationDTO } from '../../../../shared/models/use-case-response.dto';

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

    renderer = inject(Renderer2);
    router = inject(Router);
    store = inject(Store);
    formBuilder = inject(FormBuilder);
    toastrService = inject(ToastrService);
    processHeaderService = inject(ProcessHeaderService);
    receivingService = inject(ReceivingService);
    cookieService = inject(CookieService);

    form = this.formBuilder.group({
        productCategory: ['', [ Validators.required ]],
        transitTime: this.formBuilder.group({
            startDate: ['', []],
            startTime: ['', []],
            startZone: ['', []],
            endDate: ['', []],
            endTime: ['', []],
            endZone: ['', []],
        }),
        temperature: this.formBuilder.group({
            thermometerId: ['', { updateOn: 'blur' }],
            temperature: [0, [ Validators.min(-273), Validators.max(99) ]],
        }),
        comments: ['', []]
    });

    productCategoryLookupsSignal = signal<LookUpDto[]>(null);
    now = new Date();

    locationCodeComputed = computed(() => this.cookieService.get(Cookie.XFacility));
    employeeIdComputed = toSignal(this.store.select(getAuthState).pipe(map(auth => auth['id'])));
    shippingInformationSignal = signal<ShippingInformationDTO>(null);
    availableTimeZonesSignal = computed<string[]>(() => this.shippingInformationSignal()?.transitTimeZoneList?.map(lookUp => lookUp.descriptionKey) ?? []);

    thermometerStatusWithValueSignal = toSignal(this.form.controls.temperature.controls.thermometerId.statusChanges
        .pipe(combineLatestWith(this.form.controls.temperature.controls.thermometerId.valueChanges)));
    thermometerStatusWithValueChangeEffect = effect(() => {
        const [ status, value ] = this.thermometerStatusWithValueSignal();
        this.form.controls.temperature.controls.temperature.reset();
        if (status === 'VALID' && !!value) {
            this.form.controls.temperature.controls.temperature.enable();
            const temperature = this.renderer.selectRootElement('input[data-testid="temperature"]') as HTMLInputElement;
            temperature.focus();
        } else {
            this.form.controls.temperature.controls.temperature.disable();
            const thermometerIdInput = this.renderer.selectRootElement('input[data-testid="thermometer-id"]') as HTMLInputElement;
            thermometerIdInput.focus();
        }
    });

    temperatureValueSignal = toSignal(this.form.controls.temperature.controls.temperature.valueChanges.pipe(debounceTime(500)));
    temperatureQuarantineNotificationSignal = signal<UseCaseNotificationDTO>(null);
    temperatureValueChangeEffect = effect(() => {
        if (this.temperatureValueSignal()) {
            this.loadTemperatureValidation(this.temperatureValueSignal(), this.form.controls.productCategory.value)
                .subscribe(notification => this.temperatureQuarantineNotificationSignal.set(notification))
        }
    }, { allowSignalWrites: true });

    ngOnInit() {
        this.form.reset();
        this.fetchLookups().subscribe();
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

    fetchEnterShippingInformation(temperatureProductCategory: keyof typeof TemperatureProductCategory): Observable<ShippingInformationDTO> {
        return this.receivingService
            .queryEnterShippingInformation({
                productCategory: temperatureProductCategory,
                employeeId: this.employeeIdComputed(),
                locationCode: this.locationCodeComputed()
            })
            .pipe(
                catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
                tap(response => consumeUseCaseNotifications(this.toastrService, response.data?.enterShippingInformation?.notifications)),
                map((response) => {
                    const { data } = response.data.enterShippingInformation;
                    this.shippingInformationSignal.set(data);
                    return data;
                })
            );
    }

    loadTemperatureValidation(temperature: number, temperatureCategory: string) {
        return this.receivingService
            .validateTemperature({
                temperature: temperature,
                temperatureCategory: temperatureCategory,
            })
            .pipe(
                catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
                tap(response => {
                    const notifications = response.data?.validateTemperature?.notifications?.filter(n => n.type !== 'CAUTION');
                    consumeUseCaseNotifications(this.toastrService, notifications);
                }),
                map(response => {
                    return response.data?.validateTemperature?.notifications?.filter(n => n.type === 'CAUTION')?.[0];
                }),
            );
    }

    selectCategoryFromLookup(productCategoryLookup: LookUpDto): void {
        const temperatureProductCategory = productCategoryLookup.optionValue as keyof typeof TemperatureProductCategory;
        this.selectCategory(temperatureProductCategory);
    }

    selectCategory(productCategory: keyof typeof TemperatureProductCategory): void {
        this.fetchEnterShippingInformation(productCategory)
            .subscribe(shippingInformationDTO => {
                this.form.reset();
                this.updateFormValidators(shippingInformationDTO);
                this.form.controls.productCategory.setValue(productCategory);
                this.form.updateValueAndValidity();
            });
    }

    confirmThermometerId(event: Event) {
        const input = event.target as HTMLInputElement;
        input.blur();
    }

    updateFormValidators(shippingInformationDTO: ShippingInformationDTO): void {
        this.updateFormValidationForTransitTime(shippingInformationDTO.displayTransitInformation);
        this.updateFormValidationForTemperature(shippingInformationDTO.displayTemperature);
    }

    updateFormValidationForTransitTime(useTransitTime: boolean) {
        if (useTransitTime) {
            this.form.controls.transitTime.controls.startDate.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.startTime.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.startZone.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.endDate.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.endTime.setValidators([ Validators.required ]);
            this.form.controls.transitTime.controls.endZone.setValidators([ Validators.required ]);
        } else {
            this.form.controls.transitTime.controls.startDate.setValidators([]);
            this.form.controls.transitTime.controls.startTime.setValidators([]);
            this.form.controls.transitTime.controls.startZone.setValidators([]);
            this.form.controls.transitTime.controls.endDate.setValidators([]);
            this.form.controls.transitTime.controls.endTime.setValidators([]);
            this.form.controls.transitTime.controls.endZone.setValidators([]);
        }
    }

    updateFormValidationForTemperature(useTemperature: boolean) {
        if (useTemperature) {
            this.form.controls.temperature.controls.thermometerId.setValidators([ Validators.required ]);
            this.form.controls.temperature.controls.thermometerId.setAsyncValidators([ DeviceIdValidator.using(this.toastrService, this.receivingService, this.locationCodeComputed()) ]);
            this.form.controls.temperature.controls.temperature.setValidators([ Validators.required ]);
        } else {
            this.form.controls.temperature.controls.thermometerId.setValidators([]);
            this.form.controls.temperature.controls.thermometerId.setAsyncValidators([]);
            this.form.controls.temperature.controls.temperature.setValidators([]);
        }
    }

}
