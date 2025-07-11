import { Component, effect, ElementRef, inject, input, output, signal, viewChild } from '@angular/core';
import { AsyncValidatorFn, FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatError, MatFormField, MatLabel } from '@angular/material/form-field';
import { MatInput } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { toSignal } from '@angular/core/rxjs-interop';
import { combineLatestWith } from 'rxjs';
import { NotificationTypeMap } from '@shared';
import { GlobalMessageComponent } from 'app/shared/components/global-message/global-message.component';
import { UseCaseNotificationDTO } from 'app/shared/models/use-case-response.dto';

@Component({
  selector: 'biopro-temperature-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatLabel,
    MatInput,
    MatIcon,
    MatError,
    GlobalMessageComponent
  ],
  templateUrl: './temperature-form.component.html'
})
export class TemperatureFormComponent {

  protected readonly NotificationTypeMap = NotificationTypeMap;
  
  formBuilder = inject(FormBuilder);
  
  quarantineNotification = input<UseCaseNotificationDTO>(null);
  
  thermometerIdValidation = output<string>();
  temperatureValidation = output<{ temperatureProductCategory: string; temperature: number }>();
  
  thermometerField = viewChild<ElementRef<HTMLInputElement>>('thermometerId');
  temperatureField = viewChild<ElementRef<HTMLInputElement>>('temperature');
  
  formGroup = signal<FormGroup>(this.createFormGroup());
  
  thermometerIdStatusChangeSignal = toSignal(this.formGroup().controls.thermometerId.statusChanges);
  thermometerIdStatusChangeEffect = effect(() => {
    const status = this.thermometerIdStatusChangeSignal();
    this.formGroup().controls.temperature.reset();
    if (this.formGroup().controls.thermometerId.touched && status === 'VALID') {
      this.formGroup().controls.temperature.enable();
      this.temperatureField()?.nativeElement?.focus();
    } else {
      this.formGroup().controls.temperature.disable();
      if (this.formGroup().controls.thermometerId.touched) {
        this.thermometerField()?.nativeElement?.focus();
      }
    }
  });
  
  temperatureValueSignal = toSignal(this.formGroup().controls.temperature.statusChanges
    .pipe(combineLatestWith(this.formGroup().controls.temperature.valueChanges)));
  temperatureValueChangeEffect = effect(() => {
    const [status, value] = this.temperatureValueSignal();
    if (status === 'VALID' && value !== null && value !== undefined && isFinite(value)) {
      this.temperatureValidation.emit({ temperatureProductCategory: '', temperature: value });
    }
  });
  
  private createFormGroup(): FormGroup {
    return this.formBuilder.group({
      thermometerId: ['', { updateOn: 'blur' }],
      temperature: [0, { updateOn: 'blur' }],
    });
  }
  
  updateValidators(useTemperature: boolean, deviceIdAsyncValidator: AsyncValidatorFn): void {
    if (useTemperature) {
      this.formGroup().enable();
      this.formGroup().controls.thermometerId.setValidators([Validators.required]);
      this.formGroup().controls.thermometerId.setAsyncValidators([deviceIdAsyncValidator]);
      this.formGroup().controls.temperature.setValidators([Validators.required, Validators.min(-273), Validators.max(99)]);
      this.formGroup().controls.temperature.disable();
    } else {
      this.formGroup().controls.thermometerId.setValidators([]);
      this.formGroup().controls.thermometerId.setAsyncValidators([]);
      this.formGroup().controls.temperature.setValidators([]);
      this.formGroup().disable();
    }
    this.formGroup().updateValueAndValidity();
  }
  
  reset(): void {
    this.formGroup().reset();
  }
  
  triggerElementBlur(event: Event): void {
    const element = event.target as HTMLElement;
    element.blur();
  }

}
