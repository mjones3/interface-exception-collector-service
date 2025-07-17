import { CommonModule } from '@angular/common';
import {
    Component,
    ElementRef,
    EventEmitter,
    OnDestroy,
    Output,
    ViewChild,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { RsaValidators } from '@shared';
import { UppercaseDirective } from 'app/shared/directive/uppercase/uppercase.directive';
import { extractUnitNumber } from 'app/shared/utils/utils';
import { Subscription, combineLatestWith, debounceTime, filter } from 'rxjs';

@Component({
    selector: 'biopro-scan-unit-number-product-code',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        FormsModule,
        CommonModule,
        UppercaseDirective,
    ],
    templateUrl: './scan-unit-number-product-code.component.html',
})
export class ScanUnitNumberProductCodeComponent implements OnDestroy {
    @ViewChild('inputUnitNumber') inputUnitNumber: ElementRef;
    @ViewChild('inputProductCode') inputProductCode: ElementRef;
    @Output() validate: EventEmitter<{
        unitNumber: string;
        productCode: string;
    }> = new EventEmitter<{
        unitNumber: string;
        productCode: string;
    }>();
    unitProductGroup: FormGroup;
    formValueChange: Subscription;

    constructor(private fb: FormBuilder) {
        this.buildUnitProductFormGroup();
        setTimeout(() => {
            this.focusOnUnitNumber();
        }, 0);
    }

    buildUnitProductFormGroup() {
        const formGroup = this.fb.group({
            unitNumber: [
                '',
                [
                    Validators.required,
                    RsaValidators.manualEntryValidatorForUnitNumber(),
                ],
            ],
            productCode: [
                { value: '', disabled: true },
                [
                    Validators.required,
                    RsaValidators.manualEntryValidatorForProductCode(),
                ],
            ],
        });

        this.formValueChange = formGroup.statusChanges
            .pipe(
                combineLatestWith(formGroup.valueChanges),
                filter(
                    ([status, value]) =>
                        !!value.unitNumber?.trim() &&
                        !!value.productCode?.trim() &&
                        status === 'VALID'
                ),
                debounceTime(300)
            )
            .subscribe(() => this.verifyProduct());

        this.unitProductGroup = formGroup;
    }

    ngOnDestroy() {
        this.formValueChange?.unsubscribe();
    }

    get controlUnitNumber() {
        return this.unitProductGroup.controls['unitNumber'];
    }

    get controlProductCode() {
        return this.unitProductGroup.controls['productCode'];
    }

    enableProductCode(): void {
        if (this.unitProductGroup.controls.unitNumber.valid) {
            this.unitProductGroup.controls.productCode.enable();
            requestAnimationFrame(() => {
                this.focusProductCode();
            });
        } else {
            this.unitProductGroup.controls.productCode.disable();
            this.unitProductGroup.controls.productCode.reset();
        }
    }

    focusProductCode() {
        this.inputProductCode?.nativeElement.focus();
    }

    focusOnUnitNumber() {
        this.inputUnitNumber?.nativeElement.focus();
    }

    verifyProduct(): void {
        const unitNumber: string = this.controlUnitNumber?.value ?? '';
        const obj = {
            unitNumber: this.controlUnitNumber.valid
                ? extractUnitNumber(unitNumber).toUpperCase()
                : '',
            productCode: this.controlProductCode.valid
                ? this.productCodeScannedValue
                : '',
        };
        if (this.unitProductGroup.valid) {
            this.validate.emit(obj);
        }
    }

    get productCodeScannedValue(): string {
        let productCode: string = this.controlProductCode.value ?? '';
        const scanner = productCode.startsWith('=<');
        if (productCode && scanner) {
            productCode = productCode.substring(2);
        }
        return productCode;
    }

    resetUnitProductGroup(): void {
        this.unitProductGroup.reset();
        this.unitProductGroup.enable();
        this.enableProductCode();
    }

    disableUnitProductGroup(): void {
        this.unitProductGroup.reset();
        this.unitProductGroup.disable();
    }
}
