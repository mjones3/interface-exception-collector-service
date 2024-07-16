import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Output,
  ViewChild,
} from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { ScanUnitNumberCheckDigitComponent } from 'app/shared/components/scan-unit-number-check-digit/scan-unit-number-check-digit.component';
import { ControlErrorsDirective } from 'app/shared/directive/control-errors/control-errors.directive';
import { RsaValidators } from 'app/shared/forms/rsa-validators';
import {
  ValidationPipe,
  ValidationType,
} from 'app/shared/pipes/validation.pipe';
import { VerifyFilledProductDto } from '../../models/shipment-info.dto';

@Component({
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatRadioModule,
        MatFormFieldModule,
        MatInputModule,
        ControlErrorsDirective,
        ValidationPipe,
        ScanUnitNumberCheckDigitComponent,
    ],
    selector: 'rsa-enter-unit-number-product-code',
    templateUrl: './enter-unit-number-product-code.component.html',
})
export class EnterUnitNumberProductCodeComponent {
    disableCheckDigit = true;
    productGroup: FormGroup;
    unitNumberFocus = true;
    readonly validationType = ValidationType;

    @Output()
    unitNumberProductCodeSelected: EventEmitter<VerifyFilledProductDto> =
        new EventEmitter<VerifyFilledProductDto>();

    @ViewChild('unitnumber')
    unitNumberComponent: ScanUnitNumberCheckDigitComponent;

    constructor(
        protected fb: FormBuilder,
        private changeDetector: ChangeDetectorRef
    ) {
        this.productGroup = fb.group({
            unitNumber: ['', [Validators.required, RsaValidators.unitNumber]],
            productCode: [
                '',
                [RsaValidators.fullProductCode, Validators.required],
            ],
            visualInspection: [
                { value: '', disabled: true },
                [Validators.required],
            ],
        });
    }

    verifyUnit(event: {
        unitNumber: string;
        checkDigit: string;
        scanner: boolean;
    }) {
        this.productGroup.controls.unitNumber.setValue(event.unitNumber);
        this.enableVisualInspection();
    }

    onSelectVisualInspection(): void {
        if (this.productGroup.valid) {
            const visualInspection =
                this.productGroup.controls.visualInspection.value;
            if (visualInspection === 'satisfactory') {
                setTimeout(() => {
                    this.verifyProduct();
                }, 300);
            }
        }
    }

    verifyProduct(): void {
        this.unitNumberProductCodeSelected.emit({
            ...this.productGroup.value,
            productCode: this.productCode,
        });
    }

    disableProductGroup(): void {
        this.productGroup.reset();
        this.productGroup.disable();
        this.unitNumberComponent.controlUnitNumber.disable();
        this.enableVisualInspection();
    }

    resetProductFormGroup(): void {
        this.productGroup.controls.visualInspection.setValue(null);
        this.productGroup.reset();
        this.unitNumberComponent.reset();
        this.productGroup.updateValueAndValidity();
        this.changeDetector.detectChanges();
    }

    get unitNumber(): string {
        return this.productGroup.controls.unitNumber.value;
    }

    get productCode(): string {
        let productCode: string =
            this.productGroup.controls.productCode.value ?? '';
        const scanner = productCode.startsWith('=<');
        if (productCode && scanner) {
            if (scanner) {
                productCode = productCode.substring(2);
            }
            return productCode;
        }
        return this.productGroup.controls.productCode.value;
    }

    enableVisualInspection(): void {
        if (
            this.productGroup.controls.unitNumber.valid &&
            this.productGroup.controls.productCode.valid
        ) {
            this.productGroup.controls.visualInspection.enable();
        } else {
            this.productGroup.controls.visualInspection.disable();
        }
    }
}
