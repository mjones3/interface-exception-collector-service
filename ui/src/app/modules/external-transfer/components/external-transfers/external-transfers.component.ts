import { AsyncPipe, CommonModule, formatDate } from '@angular/common';
import {
    AfterViewChecked,
    ChangeDetectorRef,
    Component,
    Inject,
    LOCALE_ID,
    OnDestroy,
    OnInit,
    ViewChild,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDivider } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApolloError } from '@apollo/client';
import { FuseCardComponent } from '@fuse/components/card/public-api';
import { Store } from '@ngrx/store';
import {
    NotificationTypeMap,
    ProcessHeaderComponent,
    ProcessHeaderService,
    RsaValidators,
    ToastrImplService,
} from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { OrderService } from 'app/modules/orders/services/order.service';
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { SearchSelectComponent } from 'app/shared/components/search-select/search-select.component';
import { Subscription, catchError, take } from 'rxjs';
import { commonRegex } from '../../../../shared/utils/utils';
import { customerOptionDto } from '../../models/external-transfer.dto';
import { ExternalTransferService } from '../../services/external-transfer.service';
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
        SearchSelectComponent,
        BasicButtonComponent,
        MatDivider,
    ],
    templateUrl: './external-transfers.component.html',
})
export class ExternalTransfersComponent
    implements OnInit, AfterViewChecked, OnDestroy
{
    static readonly DATE_WITH_SLASHES = 'MM/dd/yyyy';
    static readonly DATE_WITH_SLASHES_REGEX = RegExp(
        commonRegex.dateWithSlashes
    );

    @ViewChild('enterProducts') protected enterProducts: EnterProductsComponent;
    formValueChange: Subscription;
    externalTransfer: FormGroup;
    isExternalTransferInfoValid = false;
    isTransferInfoValid = false;
    isShippedLocation = false;
    customerOptions: customerOptionDto[];
    maxDate = new Date();
    transferDate: string;

    loggedUserId: string;

    constructor(
        public header: ProcessHeaderService,
        public orderService: OrderService,
        private toaster: ToastrImplService,
        private externalTransferService: ExternalTransferService,
        protected fb: FormBuilder,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private store: Store,
        @Inject(LOCALE_ID) public locale: string
    ) {
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => {
                this.loggedUserId = auth['id'];
            });
        this.buildFormGroup();
    }

    buildFormGroup() {
        this.externalTransfer = this.fb.group({
            transferCustomer: ['', [Validators.required]],
            hospitalTransferId: [''],
            transferDate: [
                '',
                [Validators.required, RsaValidators.futureDateValidator],
            ],
        });
    }

    ngOnInit(): void {
        this.loadAllCustomerList();
    }

    ngAfterViewChecked(): void {
        this.changeDetectorRef.detectChanges();
    }

    ngOnDestroy(): void {
        this.formValueChange?.unsubscribe();
    }

    private loadAllCustomerList() {
        this.externalTransferService.customerInfo().subscribe({
            next: (response) => {
                if (Array.isArray(response?.data.findAllCustomers)) {
                    this.customerOptions = response.data.findAllCustomers;
                } else {
                    this.customerOptions = [];
                }
            },
            error: (error: ApolloError) => {
                this.toaster.error(ERROR_MESSAGE);
            },
        });
    }

    getHospitalTransferIdDisable() {
        const hospitalTransferId =
            this.externalTransfer.controls.hospitalTransferId;
        if (hospitalTransferId.value < 0 || hospitalTransferId.value !== '') {
            return hospitalTransferId.disable();
        }
    }

    createExternalTransfer() {
        const transferDateValue =
            this.externalTransfer.controls.transferDate?.value;
        const formattedTransferDate = formatDate(
            transferDateValue,
            'yyyy-MM-dd',
            this.locale
        );
        const createTransferInfo = {
            customerCode:
                this.externalTransfer.controls.transferCustomer?.value ?? '',
            hospitalTransferId:
                this.externalTransfer.controls.hospitalTransferId?.value ?? '',
            transferDate: formattedTransferDate ?? '',
            createEmployeeId: this.loggedUserId,
        };
        this.externalTransferService
            .createExternalTransferInfo(createTransferInfo)
            .pipe(
                catchError((err) => {
                    this.toaster.error(ERROR_MESSAGE);
                    throw err;
                })
            )
            .subscribe({
                next: (response) => {
                    const ruleResult = response.data?.createExternalTransfer;
                    if (ruleResult.ruleCode === '200 OK') {
                        this.isTransferInfoValid = true;
                        this.externalTransfer.controls.transferCustomer.disable();
                        this.externalTransfer.controls.transferDate.disable();
                        this.getHospitalTransferIdDisable();
                    } else {
                        const notification = ruleResult.notifications[0];
                        this.toaster.show(
                            notification?.message,
                            null,
                            null,
                            NotificationTypeMap[notification?.notificationType]
                                .type
                        );
                    }
                },
            });
    }

    onDateInput(event?: Event): void {
        const inputEvent = event as InputEvent;
        const htmlInputElement = inputEvent?.currentTarget as HTMLInputElement;
        this.transferDate = htmlInputElement?.value ?? '';
        this.checkeValidation();
    }

    checkeValidation() {
        if (
            this.externalTransfer.controls.transferDate.valid &&
            this.externalTransfer.controls.transferCustomer.valid
        ) {
            const transferDateValue =
                this.externalTransfer.controls.transferDate.value;
            this.transferDate = formatDate(
                transferDateValue.toISODate(),
                ExternalTransfersComponent.DATE_WITH_SLASHES,
                this.locale
            );
        }

        if (
            this.externalTransfer.valid &&
            ExternalTransfersComponent.DATE_WITH_SLASHES_REGEX.test(
                this.transferDate
            )
        ) {
            this.createExternalTransfer();
        }
    }

    enterProduct() {
        // TODO Implement
    }
}
