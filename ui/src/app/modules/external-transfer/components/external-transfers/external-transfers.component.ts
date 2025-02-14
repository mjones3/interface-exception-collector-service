import { AsyncPipe, CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, ViewChild, AfterViewChecked, OnDestroy } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { ProcessHeaderComponent, ProcessHeaderService } from '@shared';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { MultipleSelectComponent } from 'app/shared/components/multiple-select/multiple-select.component';
import { SearchSelectComponent } from 'app/shared/components/search-select/search-select.component';
import { Subscription, combineLatestWith, debounceTime, filter } from 'rxjs';
import { EnterProductsComponent } from '../../shared/enter-products/enter-products.component';

@Component({
    selector: 'biopro-external-transfers',
    standalone: true,
    imports: [
        CommonModule,
        FuseCardComponent,
        AsyncPipe,
        ProcessHeaderComponent,
        ActionButtonComponent,
        EnterProductsComponent,
        ReactiveFormsModule,
        MatInputModule,
        MatFormFieldModule,
        MatDatepickerModule,
        MultipleSelectComponent,
        SearchSelectComponent,
    ],
    templateUrl: './external-transfers.component.html',
})
export class ExternalTransfersComponent implements AfterViewChecked, OnDestroy {
    @ViewChild('enterProducts') protected enterProducts: EnterProductsComponent;
    formValueChange: Subscription;
    externalTransfer: FormGroup;
    isTransferInfoValid = false;
    isShippedLocation = true;
    maxDate = new Date();

    constructor(
        public header: ProcessHeaderService,
        protected fb: FormBuilder,
        private readonly changeDetectorRef: ChangeDetectorRef
    ) {
        this.buildFormGroup();
    }

    buildFormGroup() {
        const formGroup = this.fb.group({
            transferCustomer: ['', [Validators.required]],
            hospitalTransfer: [null],
            transferDate: [null, [Validators.required]],
        });

        this.formValueChange = formGroup.statusChanges
            .pipe(
                combineLatestWith(formGroup.valueChanges),
                filter(
                    ([status, value]) =>
                        !!value.transferCustomer &&
                        !!value.transferDate &&
                        status === 'VALID'
                ),
                debounceTime(300)
            )
            .subscribe(() => this.processExternalTransfer());
        this.externalTransfer = formGroup;
    }

    ngAfterViewChecked(): void {
        this.changeDetectorRef.detectChanges();
    }

    ngOnDestroy() {
        this.formValueChange?.unsubscribe();
    }

    processExternalTransfer() {
        // TODO Implement
        const transferValue = {
            transferCustomer:
                this.externalTransfer.controls.transferCustomer.value,
            hospitalTransfer:
                this.externalTransfer.controls.hospitalTransfer.value,
            transferDate: this.externalTransfer.controls.transferDate.value,
        };
        this.isTransferInfoValid = true;
        this.externalTransfer.disable();
    }

    enterProduct() {
        // TODO Implement
    }
}
