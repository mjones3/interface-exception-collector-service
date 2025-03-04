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
    computed,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
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
import { ActionButtonComponent } from 'app/shared/components/buttons/action-button.component';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { SearchSelectComponent } from 'app/shared/components/search-select/search-select.component';
import { UnitNumberCardComponent } from 'app/shared/components/unit-number-card/unit-number-card.component';
import { ProductIconsService } from 'app/shared/services/product-icon.service';
import { consumeNotifications } from 'app/shared/utils/notification.handling';
import {
    Subscription,
    catchError,
    combineLatestWith,
    debounceTime,
    filter,
    take,
} from 'rxjs';
import {
    CustomerOptionDTO,
    ExternalTransferItemDTO,
    ExternalTransferResponseDTO,
} from '../../models/external-transfer.dto';
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
        MatDividerModule,
        MatDatepickerModule,
        SearchSelectComponent,
        MatFormFieldModule,
        BasicButtonComponent,
        UnitNumberCardComponent,
    ],
    templateUrl: './external-transfers.component.html',
})
export class ExternalTransfersComponent
    implements OnInit, AfterViewChecked, OnDestroy
{
    @ViewChild('enterProductsComponent')
    protected enterProductsComponent: EnterProductsComponent;
    formValueChange: Subscription;
    createExternalTransferResponse = signal<ExternalTransferResponseDTO>(null);
    protected addedProductsComputed = computed(
        () => this.createExternalTransferResponse()?.externalTransferItems ?? []
    );

    externalTransfer: FormGroup;
    productDetails: ExternalTransferItemDTO;
    selectedProducts: ExternalTransferItemDTO[] = [];
    customerOptions: CustomerOptionDTO[];
    maxDate = new Date();
    loggedUserId: string;

    constructor(
        public header: ProcessHeaderService,
        private _router: Router,
        private toaster: ToastrImplService,
        private externalTransferService: ExternalTransferService,
        protected fb: FormBuilder,
        private readonly changeDetectorRef: ChangeDetectorRef,
        private productIconService: ProductIconsService,

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
        const formGroup = this.fb.group({
            transferCustomer: ['', [Validators.required]],
            hospitalTransferId: ['', [Validators.maxLength(250)]],
            transferDate: [
                '',
                [Validators.required, RsaValidators.futureDateValidator],
            ],
        });
        this.formValueChange = formGroup.statusChanges
            .pipe(
                combineLatestWith(formGroup.valueChanges),
                debounceTime(300),
                filter(
                    ([status, value]) =>
                        !!value.transferCustomer &&
                        !!value.transferDate &&
                        status === 'VALID'
                )
            )
            .subscribe(() => this.createExternalTransfer());
        this.externalTransfer = formGroup;
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

    disableExternalTransferForm() {
        this.externalTransfer.controls.transferCustomer.disable();
        this.externalTransfer.controls.transferDate.disable();
        this.getHospitalTransferIdDisable();
    }

    enableExternalTransferForm() {
        this.externalTransfer.controls.transferCustomer.enable();
        this.externalTransfer.controls.transferDate.enable();
        this.externalTransfer.controls.hospitalTransferId.enable();
    }

    createExternalTransfer() {
        if (!this.externalTransfer.valid) {
            return;
        }
        this.disableExternalTransferForm();
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
                        this.createExternalTransferResponse.set(
                            ruleResult.results.results[0]
                        );
                        this.disableExternalTransferForm();
                    } else {
                        this.enableExternalTransferForm();
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

    getIcon(productFamily) {
        return this.productIconService.getIconByProductFamily(productFamily);
    }

    enterProduct(item: ExternalTransferItemDTO) {
        return this.externalTransferService
            .verifyExternalTransferItem(this.getTransferProductDetail(item))
            .pipe(
                catchError((err) => {
                    this.toaster.error(ERROR_MESSAGE);
                    throw err;
                })
            )
            .subscribe((response) => {
                const ruleResult = response?.data?.addExternalTransferProduct;
                if (ruleResult) {
                    if (ruleResult.ruleCode === '200 OK') {
                        const result = ruleResult?.results?.results[0];
                        if (result) {
                            this.createExternalTransferResponse.set(result);
                            this.enterProductsComponent.resetProductGroup();
                        }
                    } else {
                        const notification = ruleResult.notifications[0];
                        this.toaster
                            .show(
                                notification?.message,
                                null,
                                null,
                                NotificationTypeMap[
                                    notification?.notificationType
                                ].type
                            )
                            .onTap.subscribe(() => {
                                this.enterProductsComponent.resetProductGroup();
                                this.enterProductsComponent.focusOnUnitNumber();
                            });
                    }
                }
            });
    }

    private getTransferProductDetail(
        item: ExternalTransferItemDTO
    ): ExternalTransferItemDTO {
        return {
            externalTransferId: this.externalTransferId(),
            unitNumber: item.unitNumber,
            productCode: item.productCode,
            employeeId: this.loggedUserId,
        };
    }

    submitExternalTransfer() {
        this.externalTransferService
            .completeExternalTransfer(this.getValidateRuleDto())
            .pipe(
                catchError((err) => {
                    this.toaster.error(ERROR_MESSAGE);
                    throw err;
                })
            )
            .subscribe({
                next: (response) => {
                    const ruleResult = response?.data?.completeExternalTransfer;
                    const notifications = ruleResult.notifications;
                    const url = ruleResult._links?.next;
                    if (notifications?.length) {
                        consumeNotifications(this.toaster, notifications);
                        if (url) {
                            this._router
                                .navigateByUrl('/', {
                                    skipLocationChange: true,
                                })
                                .then(() => {
                                    this._router.navigate([url]);
                                });
                        }
                    }
                },
            });
    }

    protected numberOfProducts = computed(
        () =>
            this.createExternalTransferResponse()?.externalTransferItems?.length
    );

    get numberOfSelectedProducts() {
        return this.selectedProducts.length;
    }

    protected addedProducts = computed(
        () => this.createExternalTransferResponse()?.externalTransferItems
    );

    get hospitalTransferId() {
        return this.externalTransfer.controls.hospitalTransferId.value;
    }

    protected lastShippedLocationComputed = computed(
        () => this.createExternalTransferResponse()?.customerFrom?.name
    );

    protected externalTransferId = computed(
        () => this.createExternalTransferResponse()?.id
    );

    private getValidateRuleDto() {
        return {
            externalTransferId: this.externalTransferId(),
            hospitalTransferId: this.hospitalTransferId,
            employeeId: this.loggedUserId,
        };
    }
}
