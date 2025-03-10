import { CommonModule } from '@angular/common';
import {
    AfterViewInit,
    ChangeDetectorRef,
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
import { commonRegex } from 'app/shared/utils/utils';
import { Subscription, combineLatestWith, debounceTime, filter } from 'rxjs';

@Component({
    selector: 'biopro-enter-products',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        FormsModule,
        CommonModule,
        UppercaseDirective,
    ],
    templateUrl: './enter-products.component.html',
})
export class EnterProductsComponent implements OnDestroy, AfterViewInit {
    productGroup: FormGroup;
    formValueChange: Subscription;
    @Output() validate: EventEmitter<{
        unitNumber: string;
        productCode: string;
    }> = new EventEmitter<{
        unitNumber: string;
        productCode: string;
    }>();
    @ViewChild('inputUnitNumber') inputUnitNumber: ElementRef;
    @ViewChild('inputProductCode') inputProductCode: ElementRef;

    constructor(
        protected fb: FormBuilder,
        private cd: ChangeDetectorRef
    ) {
        this.buildFormGroup();
    }

    buildFormGroup() {
        const formGroup = this.fb.group({
            unitNumber: [
                '',
                [
                    Validators.required,
                    Validators.pattern(commonRegex.unitNumber),
                    RsaValidators.scannedValidator(),
                ],
            ],
            productCode: [
                '',
                [
                    Validators.required,
                    Validators.pattern(commonRegex.productCode),
                    RsaValidators.scannedValidator(),
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
        this.productGroup = formGroup;
    }

    ngAfterViewInit(): void {
        setTimeout(() => {
            this.focusOnUnitNumber();
        }, 0);
    }

    ngOnDestroy() {
        this.formValueChange?.unsubscribe();
    }

    get controlUnitNumber() {
        return this.productGroup.controls['unitNumber'];
    }

    get controlProductCode() {
        return this.productGroup.controls['productCode'];
    }

    focusOnUnitNumber() {
        this.inputUnitNumber?.nativeElement?.focus();
    }

    verifyProduct(): void {
        const unitNumber: string = this.controlUnitNumber?.value ?? '';
        const productCode: string = this.controlProductCode?.value ?? '';
        if (this.productGroup.valid) {
            this.validate.emit({ unitNumber, productCode });
        }
    }

    resetProductGroup(): void {
        if (document.activeElement instanceof HTMLElement) {
            document.activeElement.blur();
        }
        this.productGroup.reset();
    }
}
