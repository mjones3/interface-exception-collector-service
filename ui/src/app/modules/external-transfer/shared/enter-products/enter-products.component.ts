import { CommonModule } from '@angular/common';
import {
    AfterContentChecked,
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    Output,
    ViewChild, OnDestroy,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
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
export class EnterProductsComponent
    implements AfterViewInit, AfterContentChecked, OnDestroy
{
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
                ],
            ],
            productCode: [
                { value: '', disabled: true },
                [
                    Validators.required,
                    Validators.pattern(commonRegex.productCode),
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

    ngAfterContentChecked(): void {
        this.cd.detectChanges();
    }

    get controlUnitNumber() {
        return this.productGroup.controls['unitNumber'];
    }

    get controlProductCode() {
        return this.productGroup.controls['productCode'];
    }

    enableProductCode(): void {
        if (this.productGroup.controls.unitNumber.valid) {
            this.productGroup.controls.productCode.enable();
            this.focusProductCode();
        } else {
            this.productGroup.controls.productCode.disable();
            this.productGroup.controls.productCode.reset();
        }
    }

    focusProductCode() {
        this.inputProductCode?.nativeElement.focus();
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
        this.productGroup.reset();
        this.productGroup.enable();
        this.enableProductCode();
    }
}
