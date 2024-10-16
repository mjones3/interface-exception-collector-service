import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    Output,
    ViewChild,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    MatButtonToggle,
    MatButtonToggleGroup,
    MatButtonToggleModule,
} from '@angular/material/button-toggle';
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
import { Subscription, catchError, filter, of } from 'rxjs';
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
        MatButtonToggleGroup,
        MatButtonToggleModule,
        MatButtonToggle,
        ScanUnitNumberCheckDigitComponent,
        MatButtonModule,
    ],
    selector: 'rsa-enter-unit-number-product-code',
    templateUrl: './enter-unit-number-product-code.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnterUnitNumberProductCodeComponent implements OnDestroy {
    productGroup: FormGroup;
    unitNumberFocus = true;

    @Input() showCheckDigit = true;
    @Output()
    unitNumberProductCodeSelected: EventEmitter<VerifyFilledProductDto> =
        new EventEmitter<VerifyFilledProductDto>();

    @ViewChild('unitnumber')
    unitNumberComponent: ScanUnitNumberCheckDigitComponent;

    @ViewChild('inputProductCode') inputProductCode: ElementRef;

    @Input() showVisualInspection = false;

    formValueChange: Subscription;

    constructor(
        protected fb: FormBuilder,
        private changeDetector: ChangeDetectorRef,
        private shipmentService: ShipmentService,
        private toaster: ToastrImplService
    ) {
        this.buildFormGroup();
    }

    buildFormGroup() {
        const formGroup = this.fb.group({
            unitNumber: ['', [Validators.required, RsaValidators.unitNumber]],
            productCode: [
                { value: '', disabled: true },
                [RsaValidators.fullProductCode, Validators.required],
            ],
            visualInspection: [
                { value: '', disabled: true },
                [
                    this.showVisualInspection
                        ? Validators.required
                        : Validators.nullValidator,
                ],
            ],
        });

        this.formValueChange = formGroup.valueChanges
            .pipe(
                filter(
                    (value) =>
                        !!value.unitNumber?.trim() &&
                        !!value.productCode?.trim() &&
                        !!value.visualInspection?.trim()
                )
            )
            .subscribe(() => this.verifyProduct());

        this.productGroup = formGroup;
    }

    ngOnDestroy() {
        this.formValueChange?.unsubscribe();
    }

    verifyUnit(event: {
        unitNumber: string;
        checkDigit: string;
        scanner: boolean;
        checkDigitChange: boolean;
    }) {
        this.productGroup.controls.unitNumber.setValue(event.unitNumber);
        this.enableProductCode();
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
        if (this.showVisualInspection) {
            this.productGroup.controls.visualInspection.setValue(null);
        }

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

    get checkDigitValid() {
        return (
            !this.unitNumberComponent.form.contains('checkDigit') ||
            (this.unitNumberComponent.form.contains('checkDigit') &&
                this.unitNumberComponent.form.controls.checkDigit.valid)
        );
    }

    get visualInspectionDisabled() {
        return this.productGroup.controls.visualInspection.disabled;
    }

    enableVisualInspection(): void {
        if (this.showVisualInspection) {
            if (
                this.productGroup.controls.unitNumber.valid &&
                this.productGroup.controls.productCode.valid &&
                this.checkDigitValid
            ) {
                this.productGroup.controls.visualInspection.enable();
            } else {
                this.productGroup.controls.visualInspection.disable();
            }
        }
    }

    enableProductCode(): void {
        if (
            this.productGroup.controls.unitNumber.valid &&
            this.checkDigitValid
        ) {
            this.productGroup.controls.productCode.enable();
            this.focusProductCode();
        } else {
            this.productGroup.controls.productCode.disable();
        }
    }

    onEnterProductCode(): void {
        if (this.showVisualInspection) {
            this.enableVisualInspection();
        } else if (
            !this.showVisualInspection &&
            this.productGroup.valid &&
            this.checkDigitValid
        ) {
            setTimeout(() => {
                this.verifyProduct();
            }, 300);
        }
    }

    focusProductCode() {
        this.inputProductCode?.nativeElement.focus();
    }
}
