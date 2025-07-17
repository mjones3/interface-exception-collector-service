import { CommonModule } from '@angular/common';
import {
    ChangeDetectionStrategy,
    ChangeDetectorRef,
    Component,
    ElementRef,
    EventEmitter,
    Input,
    OnDestroy,
    OnInit,
    Output,
    ViewChild
} from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatButtonToggle, MatButtonToggleGroup, MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatRadioModule } from '@angular/material/radio';
import { NotificationDto, RsaValidators, ScanUnitNumberCheckDigitComponent } from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import {
    catchError,
    combineLatestWith,
    debounceTime,
    distinctUntilChanged,
    filter,
    of,
    Subscription,
    switchMap,
    tap
} from 'rxjs';
import { VerifyFilledProductDto } from '../../models/shipment-info.dto';
import { ShipmentService } from '../../services/shipment.service';
import { ToastrService } from 'ngx-toastr';
import {
    SelectProductPickerModalComponent
} from '../select-product-picker-modal/select-product-picker-modal.component';
import { MatDialog } from '@angular/material/dialog';
import { Cookie } from '../../../../shared/types/cookie.enum';
import { CookieService } from 'ngx-cookie-service';
import handleApolloError from '../../../../shared/utils/apollo-error-handling';
import { consumeNotifications } from '../../../../shared/utils/notification.handling';
import { ProductResponseDTO } from '../../graphql/query-defintions/get-unlabeled-products.graphql';
import { ConfirmationAcknowledgmentService } from '../../../../shared/services/confirmation-acknowledgment.service';
import { NotificationCriteriaService } from '../../../../shared/services/notification-criteria.service';

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
    selector: 'biopro-enter-unit-number-product-code',
    templateUrl: './enter-unit-number-product-code.component.html',
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class EnterUnitNumberProductCodeComponent implements OnInit, OnDestroy {
    productGroup: FormGroup;
    formValueChange: Subscription;
    unitNumberFocus = true;
    @Input({ required: true }) shipmentId: number;
    @Input({ required: true }) shipmentItemId: number;
    @Input() showProductCode = true;
    @Input() showVisualInspection = false;
    @Input() showCheckDigit = true;
    @Output()
    unitNumberProductCodeSelected: EventEmitter<VerifyFilledProductDto> =
        new EventEmitter<VerifyFilledProductDto>();
    @ViewChild('unitnumber')
    unitNumberComponent: ScanUnitNumberCheckDigitComponent;
    @ViewChild('inputProductCode') inputProductCode: ElementRef;

    constructor(
        protected fb: FormBuilder,
        private changeDetector: ChangeDetectorRef,
        private cookieService: CookieService,
        private shipmentService: ShipmentService,
        private notificationCriteriaService: NotificationCriteriaService,
        private confirmationAcknowledgmentService: ConfirmationAcknowledgmentService,
        private toaster: ToastrService,
        private matDialog: MatDialog,
    ) {
        this.buildFormGroup();
    }

    buildFormGroup() {
        const formGroup = this.fb.group({
            unitNumber: ['', [Validators.required, RsaValidators.unitNumber]],
            productCode: [
                { value: '', disabled: true }, // Start as disabled when using product code field
                this.showProductCode
                    ? [ RsaValidators.fullProductCode, Validators.required ]
                    : [ ],
            ],
            visualInspection: [
                { value: '', disabled: true },
                this.showVisualInspection
                    ? [ Validators.required ]
                    : [ ],
            ],
        });

        this.formValueChange = formGroup.statusChanges
            .pipe(
                combineLatestWith(formGroup.valueChanges),
                distinctUntilChanged((previous, current) => previous[0] === current[0]),
                filter(
                    ([status, value]) => {
                        return !!value.unitNumber?.trim() &&
                            ( this.showProductCode ? !!value.productCode?.trim() : true) &&
                            ( this.showVisualInspection ? !!value.visualInspection?.trim() : true) &&
                            status === 'VALID'
                    }
                ),
                debounceTime(300)
            )
            .subscribe(() => {
                if (this.showProductCode) {
                    this.verifyProduct();
                } else {
                    this.openSelectProductDialog();
                }
            });

        this.productGroup = formGroup;
    }

    ngOnInit() {
        this.enableProductCode();
        this.enableVisualInspection();
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
        if (
            this.containCheckDigit &&
            event.checkDigitChange &&
            event.checkDigit.trim() !== ''
        ) {
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
                    this.enableProductCode();
                    this.enableVisualInspection();
                });
        } else {
            this.enableProductCode();
            this.enableVisualInspection();
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
        if (this.productGroup.valid) {
            this.unitNumberProductCodeSelected.emit({
                ...this.productGroup.value,
                productCode: this.productCode,
            });
        }
    }

    disableProductGroup(): void {
        this.productGroup.reset();
        this.productGroup.disable();
        this.unitNumberComponent.controlUnitNumber.disable();
        this.enableVisualInspection();
    }

    enableProductGroup(): void {
        this.unitNumberComponent.controlUnitNumber.enable();
        this.productGroup.enable();
        this.resetProductFormGroup();
    }

    resetProductFormGroup(): void {
        if (this.showVisualInspection) {
            this.productGroup.controls.visualInspection.setValue(null);
            this.enableVisualInspection();
        }
        this.enableProductCode();
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

    get containCheckDigit() {
        return this.unitNumberComponent.form.contains('checkDigit');
    }

    get checkDigitValid() {
        return (
            !this.containCheckDigit ||
            (this.containCheckDigit &&
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
            if (this.showProductCode) {
                this.productGroup.controls.productCode.disable();
            }
            this.productGroup.controls.productCode.reset();
        }
    }

    focusProductCode() {
        this.inputProductCode?.nativeElement.focus();
    }

    openSelectProductDialog() {
        this.shipmentService
            .getUnlabeledProducts({
                unitNumber: this.productGroup.controls.unitNumber.value,
                locationCode: this.cookieService.get(Cookie.XFacility),
                shipmentItemId: this.shipmentItemId
            })
            .pipe(
                catchError((e) => handleApolloError(this.toaster, e)),
                tap((result) => {
                    if (result?.data?.getUnlabeledProducts?.ruleCode !== '200 OK') {
                        const notifications: NotificationDto[] = [ ...(result?.data?.getUnlabeledProducts?.notifications ?? []) ];
                        if (notifications?.length) {
                            const infoNotification = this.notificationCriteriaService.filterOutByCriteria(notifications, { notificationType: 'INFO' })?.[0]
                            if (infoNotification) {
                                return this.confirmationAcknowledgmentService.openAcknowledgmentDialog(infoNotification.message, infoNotification.details);
                            }
                        }
                    }

                    consumeNotifications(
                        this.toaster,
                        result?.data?.getUnlabeledProducts?.notifications
                    );
                }),
                switchMap(result => {
                    const ruleCode = result?.data?.getUnlabeledProducts?.ruleCode;
                    const products = result.data?.getUnlabeledProducts?.results?.results?.[0];

                    // If nothing found
                    if (ruleCode !== '200 OK' || !products?.length) {
                        return of(null);
                    }

                    // If only one is available, autoselect first product
                    if (products?.length === 1) {
                        return of(products?.[0]);
                    }

                    // Show dialog asking the user to select the product
                    return this.matDialog
                        .open<SelectProductPickerModalComponent, ProductResponseDTO[], ProductResponseDTO>(
                            SelectProductPickerModalComponent, {
                                data: products
                            })
                        .afterClosed()
                }),
            )
            .subscribe(result => {
                if (result) {
                    this.productGroup.controls.productCode.setValue(result.productCode);
                    this.verifyProduct();
                    return;
                }
                this.productGroup.controls.unitNumber.setValue(null);
                this.resetProductFormGroup();
            });
    }

}
