import { CommonModule } from '@angular/common';
import {
    Component,
    ElementRef,
    EventEmitter,
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

@Component({
    selector: 'biopro-scan-unit-number-product-code',
    standalone: true,
    imports: [ReactiveFormsModule, MatInputModule, FormsModule, CommonModule],
    templateUrl: './scan-unit-number-product-code.component.html',
})
export class ScanUnitNumberProductCodeComponent {
    @ViewChild('inputUnitNumber') inputUnitNumber: ElementRef;
    @ViewChild('inputProductCode') inputProductCode: ElementRef;
    @Output() validate: EventEmitter<{ unitNumber: string }> =
        new EventEmitter<{
            unitNumber: string;
            productCode: string;
        }>();
    unitProductGroup: FormGroup;

    constructor(private fb: FormBuilder) {
        this.unitProductGroup = this.fb.group({
            unitNumber: [
                null,
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
        setTimeout(() => {
            this.focusOnUnitNumber();
        }, 0);
    }

    get controlUnitNumber() {
        return this.unitProductGroup.controls['unitNumber'];
    }

    get isScanner() {
        return !!this.controlUnitNumber.value?.startsWith('=');
    }

    enableProductCode(): void {
        if (this.unitProductGroup.controls.unitNumber.valid) {
            this.unitProductGroup.controls.productCode.enable();
            this.focusProductCode();
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
        if (this.unitProductGroup.valid) {
            this.validate.emit(this.unitProductGroup.value);
        }
    }

    resetUnitProductGroup(): void {
        this.unitProductGroup.reset();
        this.focusOnUnitNumber();
        this.enableProductCode();
    }

    disableUnitProductGroup(): void {
        this.unitProductGroup.reset();
        this.unitProductGroup.disable();
    }
}
