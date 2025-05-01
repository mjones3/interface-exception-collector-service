import { CommonModule, formatDate } from '@angular/common';
import {
    Component,
    Inject,
    LOCALE_ID,
    OnDestroy,
    OnInit,
    signal,
} from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {
    MatDialogActions,
    MatDialogModule,
    MatDialogRef,
} from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { ApolloError } from '@apollo/client';
import { Store } from '@ngrx/store';
import { ToastrImplService } from '@shared';
import { ERROR_MESSAGE } from 'app/core/data/common-labels';
import { getAuthState } from 'app/core/state/auth/auth.selectors';
import { BasicButtonComponent } from 'app/shared/components/buttons/basic-button.component';
import { SearchSelectComponent } from 'app/shared/components/search-select/search-select.component';
import { cartonWeightValidator } from 'app/shared/forms/biopro-validators';
import { OptionDTO } from 'app/shared/models/option.dto';
import { Cookie } from 'app/shared/types/cookie.enum';
import { consumeUseCaseNotifications } from 'app/shared/utils/notification.handling';
import { map } from 'lodash-es';
import { CookieService } from 'ngx-cookie-service';
import {
    Subscription,
    catchError,
    debounceTime,
    finalize,
    take,
    throwError,
} from 'rxjs';
import { RecoveredPlasmaCustomerDTO } from '../../graphql/query-definitions/customer.graphql';
import { CreateShipmentRequestDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaShipmentService } from '../../services/recovered-plasma-shipment.service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';

@Component({
    selector: 'biopro-create-shipment',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        CommonModule,
        MatIconModule,
        MatSelectModule,
        MatButtonModule,
        MatDatepickerModule,
        MatDialogModule,
        MatDialogActions,
        BasicButtonComponent,
        SearchSelectComponent,
    ],
    templateUrl: './create-shipment.component.html',
})
export class CreateShipmentComponent implements OnInit, OnDestroy {
    customerOptionList: RecoveredPlasmaCustomerDTO[] = [];
    productTypeOptions: OptionDTO[] = [];
    createShipmentForm: FormGroup;
    customerValueChange: Subscription;
    minDate = new Date();
    loggedUserId: string;
    isSubmitting = signal(false);
    constructor(
        private fb: FormBuilder,
        public dialogRef: MatDialogRef<CreateShipmentComponent>,
        private router: Router,
        private toastr: ToastrImplService,
        private store: Store,
        private cookieService: CookieService,
        private shipmentService: RecoveredPlasmaShipmentService,
        private recoveredPlasmaService: RecoveredPlasmaService,
        @Inject(LOCALE_ID) public locale: string
    ) {
        this.setLoggedUserId();
    }

    ngOnInit(): void {
        this.initializeForm();
        this.setupCustomerValueChangeSubscription();
        this.loadCustomers();
    }

    ngOnDestroy(): void {
        this.customerValueChange?.unsubscribe();
    }

    private setLoggedUserId() {
        this.store
            .select(getAuthState)
            .pipe(take(1))
            .subscribe((auth) => {
                this.loggedUserId = auth['id'];
            });
    }

    private initializeForm(): void {
        this.createShipmentForm = this.fb.group({
            customerName: ['', [Validators.required]],
            productType: [{ value: '', disabled: true }, [Validators.required]],
            cartonTareWeight: [
                '',
                [Validators.required, cartonWeightValidator()],
            ],
            shipmentDate: [''],
            transportationReferenceNumber: [''],
        });
    }

    private setupCustomerValueChangeSubscription() {
        this.customerValueChange = this.createShipmentForm
            .get('customerName')
            .valueChanges.pipe(debounceTime(300))
            .subscribe((value) => {
                if (value) {
                    this.createShipmentForm.get('productType').enable();
                    this.createShipmentForm.get('productType').setValue('');
                    this.shipmentService
                        .getProductTypeOptions(value)
                        .subscribe((result) => {
                            if (
                                Array.isArray(
                                    result?.data?.findAllProductTypeByCustomer
                                )
                            ) {
                                this.productTypeOptions = map(
                                    result.data.findAllProductTypeByCustomer,
                                    (item) => {
                                        return {
                                            code: item.productType,
                                            name: item.productTypeDescription,
                                        };
                                    }
                                );
                                if (this.productTypeOptions.length === 1) {
                                    const singleOption =
                                        this.productTypeOptions[0];
                                    this.createShipmentForm
                                        .get('productType')
                                        .setValue(singleOption.code);
                                }
                            } else {
                                this.productTypeOptions = [];
                            }
                        });
                } else {
                    this.createShipmentForm.get('productType').disable();
                    this.createShipmentForm.get('productType').setValue('');
                    this.productTypeOptions = [];
                }
            });
    }

    private loadCustomers(): void {
        this.recoveredPlasmaService.findAllCustomers().subscribe({
            next: (response) => {
                if (Array.isArray(response?.data?.findAllCustomers)) {
                    this.customerOptionList = response?.data.findAllCustomers;
                } else {
                    this.customerOptionList = [];
                }
            },
            error: (error: ApolloError) => {
                this.toastr.error(ERROR_MESSAGE);
                throw error;
            },
        });
    }

    get createShipmentFormControl() {
        return this.createShipmentForm.controls;
    }

    submit(): void {
        if (!this.createShipmentForm.valid) {
            return;
        }
        if (this.createShipmentForm.valid) {
            this.isSubmitting.set(true);
            this.shipmentService
                .createRecoveredPlasmaShipment(this.prepareShipmentData())
                .pipe(
                    finalize(() => {
                        this.isSubmitting.set(false);
                    }),
                    catchError((err) => {
                        this.toastr.error(ERROR_MESSAGE);
                        return throwError(() => err);
                    })
                )
                .subscribe({
                    next: (response) => {
                        const ruleResult = response?.data?.createShipment;
                        const url = ruleResult._links?.next;
                        const notifications = ruleResult.notifications;
                        if (notifications?.length) {
                            consumeUseCaseNotifications(
                                this.toastr,
                                notifications
                            );
                            this.dialogRef.close(true);
                            if (notifications[0].type === 'SUCCESS') {
                                if (url) {
                                    this.handleNavigation(url);
                                }
                            }
                        }
                    },
                });
        }
    }

    private prepareShipmentData(): CreateShipmentRequestDTO {
        const shipmentDate = this.createShipmentFormControl.shipmentDate.value
            ? this.formatShipmentDate(this.createShipmentFormControl.shipmentDate.value)
            : null;

        return {
            locationCode: this.cookieService.get(Cookie.XFacility),
            createEmployeeId: this.loggedUserId,
            customerCode: this.createShipmentFormControl.customerName?.value ?? '',
            productType: this.createShipmentFormControl.productType?.value ?? '',
            cartonTareWeight: this.parseCartonWeight(),
            ...(shipmentDate ? { shipmentDate } : {}),
            transportationReferenceNumber: this.createShipmentFormControl.transportationReferenceNumber?.value ?? '',
        };
    }

    private formatShipmentDate(date: Date): string {
        return formatDate(date, 'yyyy-MM-dd', this.locale);
    }

    private parseCartonWeight(): number {
        return parseFloat(
            this.createShipmentFormControl.cartonTareWeight?.value ?? ''
        );
    }

    private handleNavigation(url: string): void {
        this.router.navigateByUrl(url);
    }
}
