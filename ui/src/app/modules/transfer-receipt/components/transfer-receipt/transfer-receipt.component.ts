import { AsyncPipe } from '@angular/common';
import { ChangeDetectorRef, Component, computed, ElementRef, inject, signal, viewChild } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatDatepickerToggle, MatDatepicker, MatDatepickerInput } from '@angular/material/datepicker';
import { MatError, MatFormField, MatHint, MatLabel, MatSuffix } from '@angular/material/form-field';
import { MatIcon } from '@angular/material/icon';
import { MatInput } from '@angular/material/input';
import { MatSelect } from '@angular/material/select';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { LookUpDto, NotificationTypeMap, ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { CookieService } from 'ngx-cookie-service';
import { Store } from '@ngrx/store';
import { Router } from '@angular/router';
import { Cookie } from 'app/shared/types/cookie.enum';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { catchError, first, map, Observable, tap } from 'rxjs';
import { UseCaseNotificationDTO } from 'app/shared/models/use-case-response.dto';
import { TransferInformationDTO } from '../../models/internal-transfer-order.dto';
import { ToastrService } from 'ngx-toastr';
import { TransitTimeFormComponent } from 'app/shared/components/transit-time-form/transit-time-form.component';
import { ApolloError } from '@apollo/client';
import handleApolloError from 'app/shared/utils/apollo-error-handling';
import { consumeUseCaseNotifications } from 'app/shared/utils/notification.handling';
import { TemperatureFormComponent } from 'app/shared/components/temperature-form/temperature-form.component';
import { ReceivingService } from 'app/modules/imports/service/receiving.service';
import { TransferReceiptService } from '../../services/transfer-receipt.service';
import { DeviceIdValidator } from 'app/shared/forms/device-id.validator';
import { TemperatureDeviceService } from 'app/shared/services/temperature-device.service';
import { GlobalMessageComponent } from 'app/shared/components/global-message/global-message.component';
import { ProductCategoryMap } from '../../../../shared/models/product-category.model';

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
    GlobalMessageComponent
  ],
  templateUrl: './transfer-receipt.component.html'
})
export class TransferReceiptComponent {
  protected readonly NotificationTypeMap = NotificationTypeMap;
  readonly commentsMaxLength: number = 250;

  processHeaderService = inject(ProcessHeaderService);
  formBuilder = inject(FormBuilder);
  cookieService = inject(CookieService);
  router = inject(Router);
  store = inject(Store);
  receivingService = inject(ReceivingService);
  toastrService = inject(ToastrService);
  transferReceiptService = inject(TransferReceiptService);
  cdr = inject(ChangeDetectorRef);
  deviceValidatorService = inject(TemperatureDeviceService)


  transitTimeFormComponent = viewChild<TransitTimeFormComponent>('transitTimeForm');
  temperatureFormComponent = viewChild<TemperatureFormComponent>('temperatureForm');

  transferInformationForm = this.formBuilder.group({
    transferOrderNumber: ['', [ Validators.required,Validators.pattern(/^[0-9]+$/)]],
    temperatureCategory: [{value: '', disabled: true}],
    comments: ['']
  });

  locationCodeComputed = computed(() => this.cookieService.get(Cookie.XFacility));
  employeeIdComputed = toSignal(this.store.select(getAuthState).pipe(map(auth => auth['id'])));
  availableTimeZonesSignal = computed<LookUpDto[]>(() => this.transferInformationSignal()?.transitTimeZoneList ?? []);

  transferInformationSignal = signal<TransferInformationDTO>(null);
  transitTimeHumanReadableSignal = signal<string>(null);
  transitTimeQuarantineSignal = signal<UseCaseNotificationDTO>(null);
  transferOrderNumberSignal = toSignal(this.transferInformationForm.controls.transferOrderNumber.valueChanges);
  isDifferentLocationSignal = signal<boolean>(false);

  temperatureQuarantineSignal = signal<UseCaseNotificationDTO>(null);
  thermometerField = viewChild<ElementRef<HTMLInputElement>>('thermometerId');
  temperatureField = viewChild<ElementRef<HTMLInputElement>>('temperature');
  now = new Date();

  cancel(): void {
    this.transferInformationForm.reset();
    this.transitTimeFormComponent()?.reset();
    this.temperatureFormComponent()?.reset();
  }

  onEnterTransferOrder(): void {
    if(this.transferInformationForm.controls.transferOrderNumber.valid){
        const orderNumber = +this.transferInformationForm.controls.transferOrderNumber.value;
        this.transitTimeFormComponent()?.reset();
        this.temperatureFormComponent()?.reset();
        this.triggerQueryTransferOrderNumber(orderNumber)
            .subscribe(transferInformationDTO => {
                this.updateFormValidators(transferInformationDTO);
                this.isDifferentLocationSignal.set(transferInformationDTO.receivedDifferentLocation)
                this.transferInformationForm.controls.temperatureCategory.setValue(ProductCategoryMap[transferInformationDTO.productCategory].toUpperCase());
                this.transferInformationForm.controls.transferOrderNumber.setValue(transferInformationDTO.orderNumber);
                this.cdr.detectChanges();
                this.transitTimeFormComponent()?.setEndZone(transferInformationDTO.defaultTimeZone);
                this.transitTimeFormComponent()?.setStartZone(transferInformationDTO.defaultStartTimeZone);
                this.transferInformationForm.updateValueAndValidity();
            });
    }
  }

  triggerQueryTransferOrderNumber(orderNumber: number): Observable<TransferInformationDTO> {
    return this.transferReceiptService
        .validateTransferOrderNumber({
            orderNumber: orderNumber,
            employeeId: this.employeeIdComputed(),
            locationCode: this.locationCodeComputed()
        })
        .pipe(
            first(),
            catchError((error: ApolloError) => handleApolloError(this.toastrService, error)),
            tap(response => consumeUseCaseNotifications(this.toastrService, response.data?.validateTransferOrderNumber?.notifications)),
            map((response) => {
                const { data } = response.data.validateTransferOrderNumber;
                this.transferInformationSignal.set(data);
                return data;
            })
        );
  }

  updateFormValidators(transferInformationDTO: TransferInformationDTO): void {
    this.updateFormValidationForTransitTime(transferInformationDTO.displayTransitInformation);
    this.updateFormValidationForTemperature(transferInformationDTO.displayTemperature);
    this.updateValidtatorForComments(transferInformationDTO.receivedDifferentLocation);
  }

  updateValidtatorForComments(isCommentRequired: boolean){
    this.transferInformationForm.controls.comments.clearValidators();
    if (isCommentRequired) {
        this.transferInformationForm.controls.comments.addValidators([Validators.required]);
    }
    this.transferInformationForm.controls.comments.updateValueAndValidity();
  }

  updateFormValidationForTransitTime(useTransitTime: boolean) {
    this.transitTimeFormComponent()?.updateValidators(useTransitTime);
  }

  updateFormValidationForTemperature(useTemperature: boolean) {
    const deviceIdValidator = DeviceIdValidator.asyncValidatorUsing(this.toastrService, this.deviceValidatorService, this.locationCodeComputed());
    setTimeout(() => {
        this.temperatureFormComponent()?.updateValidators(useTemperature, deviceIdValidator);
    });
  }

  triggerElementBlur(event: Event): void {
    const element = event.target as HTMLElement;
    element.blur();
  }

  isFormValid(): boolean {
    const internalTransferFormValid = this.transferInformationForm.valid;
    const transitTimeValid = !this.transferInformationSignal()?.displayTransitInformation || this.transitTimeFormComponent()?.formGroup().valid;
    const temperatureValid = !this.transferInformationSignal()?.displayTemperature || this.temperatureFormComponent()?.formGroup().valid;
    return internalTransferFormValid && transitTimeValid && temperatureValid && this.transferInformationSignal()?.orderNumber != null;
  }

  updateTransitTimeQuarantine(data: UseCaseNotificationDTO){
    this.transitTimeQuarantineSignal.set(data);
  }

  updateTransitTimeHumanReadable(data: string){
      this.transitTimeHumanReadableSignal.set(data);
  }

  updateTemperatureQuarantine(data: UseCaseNotificationDTO){
      this.temperatureQuarantineSignal.set(data);
  }
}
