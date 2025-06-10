import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { ActionButtonComponent } from '../../../../shared/components/buttons/action-button.component';
import { AsyncPipe } from '@angular/common';
import { FuseCardComponent } from '../../../../../@fuse';
import { LookUpDto, ProcessHeaderComponent, ProcessHeaderService } from '@shared';
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
import { catchError, combineLatestWith, map, Observable, tap } from 'rxjs';
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
import { DeviceIdValidator } from '../../validators/deviceIdValidator';
import { ToastrService } from 'ngx-toastr';

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
        MatError
    ],
  templateUrl: './imports-enter-shipment-information.component.html',
    styleUrls: [ './imports-enter-shipment-information.component.scss' ],
})
export class ImportsEnterShipmentInformationComponent implements OnInit {

    protected readonly TemperatureProductCategoryIconMap = TemperatureProductCategoryIconMap;

    router = inject(Router);
    formBuilder = inject(FormBuilder);
    toastr = inject(ToastrService);
    header = inject(ProcessHeaderService);
    store = inject(Store);
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
            endZone: ['', []]
        }),
        temperature: this.formBuilder.group({
            thermometerId: ['', []],
            temperature: [0, []],
        }),
        comments: ['', []]
    });

    productCategoriesSignal = signal<LookUpDto[]>(null);
    now = new Date();

    locationCodeComputed = computed(() => this.cookieService.get(Cookie.XFacility));
    employeeIdComputed = toSignal(this.store.select(getAuthState).pipe(map(auth => auth['id'])));
    shippingInformationSignal = signal<ShippingInformationDTO>(null);
    availableTimeZonesSignal = computed<string[]>(() => this.shippingInformationSignal()?.transitTimeZoneList?.map(lookUp => lookUp.descriptionKey) ?? []);

    thermometerStatusWithValue = toSignal(this.form.controls.temperature.controls.thermometerId.statusChanges
        .pipe(combineLatestWith(this.form.controls.temperature.controls.thermometerId.valueChanges)));

    thermometerStatusWithValueChangeEffect = effect(() => {
        const [ status, value ] = this.thermometerStatusWithValue();
        if (status === 'VALID' && !!value) {
            this.form.controls.temperature.controls.temperature.enable();
        } else {
            this.form.controls.temperature.controls.temperature.disable()
        }
    });

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
                catchError((error: ApolloError) => handleApolloError(this.toastr, error)),
                map(response => {
                    this.productCategoriesSignal.set(response.data.findAllLookupsByType)
                    return response.data.findAllLookupsByType;
                })
            );
    }

    fetchEnterShippingInformation(productCategory: string): Observable<ShippingInformationDTO> {
        return this.receivingService
            .queryEnterShippingInformation({
                productCategory: productCategory,
                employeeId: this.employeeIdComputed(),
                locationCode: this.locationCodeComputed()
            })
            .pipe(
                catchError((error: ApolloError) => handleApolloError(this.toastr, error)),
                tap(response => consumeUseCaseNotifications(this.toastr, response.data?.enterShippingInformation?.notifications)),
                map((response) => {
                    const { data } = response.data.enterShippingInformation;
                    this.shippingInformationSignal.set(data);
                    return data;
                })
            );
    }

    selectCategory(productCategory: string): void {
        this.fetchEnterShippingInformation(productCategory)
            .subscribe(shippingInformationDTO => {
                this.form.reset();
                this.updateFormValidators(shippingInformationDTO);
                this.form.controls.productCategory.setValue(productCategory);
                this.form.updateValueAndValidity();
            });
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
            this.form.controls.temperature.controls.thermometerId.setAsyncValidators([ DeviceIdValidator.using(this.toastr, this.receivingService, this.locationCodeComputed()) ]);
            this.form.controls.temperature.controls.temperature.setValidators([ Validators.required ]);
        } else {
            this.form.controls.temperature.controls.thermometerId.setValidators([]);
            this.form.controls.temperature.controls.thermometerId.setAsyncValidators([]);
            this.form.controls.temperature.controls.temperature.setValidators([]);
        }
    }

}
