import { CommonModule } from '@angular/common';
import {
    ChangeDetectorRef,
    Component,
    EventEmitter,
    Input,
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
import {
    RsaValidators,
    ScanUnitNumberCheckDigitComponent,
    ToastrImplService,
} from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { catchError, of } from 'rxjs';
import { VerifyFilledProductDto } from '../../models/shipment-info.dto';
import { ShipmentService } from '../../services/shipment.service';

@Component({
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatRadioModule,
        MatFormFieldModule,
        MatInputModule,
        ScanUnitNumberCheckDigitComponent,
    ],
    selector: 'rsa-enter-unit-number-product-code',
    templateUrl: './enter-unit-number-product-code.component.html',
})
export class EnterUnitNumberProductCodeComponent {
    productGroup: FormGroup;
    unitNumberFocus = true;

    @Input() showCheckDigit = true;
    @Output()
    unitNumberProductCodeSelected: EventEmitter<VerifyFilledProductDto> =
        new EventEmitter<VerifyFilledProductDto>();

    @ViewChild('unitnumber')
    unitNumberComponent: ScanUnitNumberCheckDigitComponent;

    constructor(
        protected fb: FormBuilder,
        private changeDetector: ChangeDetectorRef,
        private shipmentService: ShipmentService,
        private toaster: ToastrImplService
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
        checkDigitChange: boolean;
    }) {
        this.productGroup.controls.unitNumber.setValue(event.unitNumber);
        this.enableVisualInspection();
        if (event.checkDigitChange && event.checkDigit !== '') {
            const $checkDigitVerification =
                this.showCheckDigit && !event.scanner
                    ? this.shipmentService.validateCheckDigit(
                          event.unitNumber,
                          event.checkDigit
                      )
                    : of(null);

            $checkDigitVerification
                .pipe(
                    catchError((err) => {
                        this.toaster.error(ERROR_MESSAGE);
                        throw err;
                    })
                )
                .subscribe((response) => {
                    this.checkDigitFieldError(response.data.verifyCheckDigit);
                    this.enableVisualInspection();
                });
        }
    }

    private checkDigitFieldError(response: RuleResponseDTO) {
        const valid =
            null !== response?.results && null === response?.notifications;
        this.unitNumberComponent.setValidatorsForCheckDigit(valid);
        if (!valid) {
            const invalidMessage = response.notifications.map(
                (mes) => mes.message
            );
            this.unitNumberComponent.checkDigitInvalidMessage =
                invalidMessage[0];
            this.unitNumberComponent.focusOnCheckDigit();
        }
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
        console.log(
            !this.unitNumberComponent.form.contains('checkDigit') ||
                (this.unitNumberComponent.form.contains('checkDigit') &&
                    this.unitNumberComponent.form.controls.checkDigit.valid)
        );
        if (
            this.productGroup.controls.unitNumber.valid &&
            this.productGroup.controls.productCode.valid &&
            (!this.unitNumberComponent.form.contains('checkDigit') ||
                (this.unitNumberComponent.form.contains('checkDigit') &&
                    this.unitNumberComponent.form.controls.checkDigit.valid))
        ) {
            this.productGroup.controls.visualInspection.enable();
        } else {
            this.productGroup.controls.visualInspection.disable();
        }
    }
}
