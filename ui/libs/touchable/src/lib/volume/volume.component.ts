import { Component, EventEmitter, Input, OnInit, Output, ViewEncapsulation } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { EnvironmentConfigService, ValidationType, VolumeData } from '@rsa/commons';
import { KeyboardTypeEnum } from '../../shared/models/keyboard-type.enum';

@Component({
  selector: 'rsa-volume',
  exportAs: 'rsaVolume',
  templateUrl: './volume.component.html',
  styleUrls: ['./volume.component.scss'],
  encapsulation: ViewEncapsulation.None,
})
export class VolumeComponent implements OnInit {
  @Input() headerTitle: string;
  @Input() subTitleInstructions: string;
  @Input() disabledVolume = false;
  @Output() calculateVolumeFormSubmitted = new EventEmitter<VolumeData>();
  @Output() invalidSubmitButton = new EventEmitter<boolean>();

  form: FormGroup;
  readonly keyboardType = KeyboardTypeEnum;
  readonly validationType = ValidationType;
  inputFocus = false;
  minVolumeAllowed: number;
  maxVolumeAllowed: number;

  constructor(private formBuilder: FormBuilder, public config: EnvironmentConfigService) {}

  ngOnInit(): void {
    this.initializeCalculateVolumeForm();
  }

  setVolumeFocus() {
    this.inputFocus = true;
  }

  /**
   * Initialize the form
   */
  initializeCalculateVolumeForm(): void {
    this.loadDynamicFormValidations();
    const validators = [
      Validators.required,
      Validators.pattern('^[0-9]+$'),
      Validators.min(this.minVolumeAllowed),
      Validators.max(this.maxVolumeAllowed),
    ];

    this.form = this.formBuilder.group({
      volume: [{ value: null, disabled: true }, validators],
    });
    if (!this.disabledVolume) {
      this.form.addControl('weight', new FormControl({ value: null, disabled: true }, validators));
      this.form.addControl('useIntegratedScale', new FormControl(true));
    } else {
      this.form.disable();
    }

    this.form.statusChanges.subscribe(changes => {
      if (changes === 'INVALID') {
        this.invalidSubmitButton.next(false);
      }
    });

    console.log('this.form', this.form.value);
  }

  /**
   * To load configurations from DB configurations
   */
  loadDynamicFormValidations() {
    this.minVolumeAllowed = +this.config.env.properties['MIN_VOLUME_ALLOWED'];
    this.maxVolumeAllowed = +this.config.env.properties['MAX_VOLUME_ALLOWED'];
  }

  checkFormValidity() {
    return this.form.valid;
  }

  calculateVolume(): void {
    if (this.checkFormValidity()) {
      console.log('this.form.value', this.form.value);
      this.calculateVolumeFormSubmitted.emit(this.form.value);
    }
  }

  /**
   * This method have to be call after calculateVolume in the parent component to set volume and weightToVolume then
   * on parent component
   * @param weight
   */
  setVolumeFromWeight(weight: number) {
    this.form.patchValue({ weight: weight });

    this.form.patchValue({ volume: this.getVolumeFromWeight(weight) });
  }

  setVolume(volume: number) {
    this.form.patchValue({ volume: volume });
  }

  setWeight(weight: number) {
    this.form.patchValue({ weight: weight });
  }

  private getVolumeFromWeight(weight: number) {
    // TODO check how it is calculated the volume from weight
    // we just have a static calculation.
    return this.form.get('weight').value * 2;
  }

  resetVolumeForm() {
    this.form.reset({ weight: null, volume: null, useIntegratedScale: true });
  }

  useIntegratedScaleChange($event: any) {
    this.invalidSubmitButton.next(false);
    this.form.get('weight').reset(null);
    this.form.get('volume').reset(null);
    if (!$event) {
      this.form.get('weight').enable();
    } else {
      this.form.get('weight').disable();
    }
  }

  enableWeightControl() {
    this.form.patchValue({ useIntegratedScale: false });
  }

  changeWeightModel($event: any) {
    // this.form.patchValue({ volume: this.getVolumeFromWeight($event) });
  }

  enableCalculateVolume(form: any) {
    if (form.get('useIntegratedScale')?.value) {
      return false;
    }
    return !form.get('weight')?.valid;
  }
}
