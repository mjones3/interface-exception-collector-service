import { ComponentFixture, fakeAsync, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import {
    MAT_DATE_FORMATS,
    MAT_NATIVE_DATE_FORMATS,
    MatNativeDateModule,
} from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { MutationResult } from '@apollo/client';
import { FuseConfirmationService } from '@fuse/services/confirmation';
import { provideMockStore } from '@ngrx/store/testing';
import { NotificationDto, ToastrImplService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ExternalTransferItemDTO } from '../../models/external-transfer.dto';
import { ExternalTransferService } from '../../services/external-transfer.service';
import { ExternalTransfersComponent } from './external-transfers.component';
import { AuthState } from 'app/core/state/auth/auth.reducer';

describe('ExternalTransfersComponent', () => {
    let component: ExternalTransfersComponent;
    let fixture: ComponentFixture<ExternalTransfersComponent>;
    let dateInput: HTMLInputElement;
    let service: ExternalTransferService;
    let toastr: ToastrImplService;
    const routerMock = {
        navigateByUrl: jest.fn(() => Promise.resolve(true)),
        navigate: jest.fn(),
    };
    let fuseConfirmationService: FuseConfirmationService;

    const addedProductsMockData = {
        unitNumber: 'W036898786807',
        productCode: 'E0869V00',
        employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
        productFamily: 'PLASMA_TRANSFUSABLE',
        externalTransferId: 1,
    };

    const initialState: AuthState = {
        id: 'mock-user-id',
        loaded: true,
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ExternalTransfersComponent,
                ApolloTestingModule,
                MatDatepickerModule,
                MatNativeDateModule,
                MatInputModule,
                MatFormFieldModule,
                ReactiveFormsModule,
                NoopAnimationsModule,
                MatSnackBarModule,
                ToastrModule.forRoot(),
            ],
            providers: [
                ExternalTransferService,
                provideMockStore({initialState}),
                {
                    provide: MAT_DATE_FORMATS,
                    useValue: MAT_NATIVE_DATE_FORMATS,
                },
                {
                    provide: Router,
                    useValue: routerMock,
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ExternalTransfersComponent);
        component = fixture.componentInstance;
        service = TestBed.inject(ExternalTransferService);
        jest.spyOn(service, 'customerInfo').mockReturnValue(of());
        jest.spyOn(service, 'verifyExternalTransferItem').mockReturnValue(of());
        jest.spyOn(service, 'completeExternalTransfer').mockReturnValue(of());
        toastr = TestBed.inject(ToastrImplService);
        fixture.detectChanges();
        dateInput = fixture.debugElement.query(By.css('input')).nativeElement;
        fuseConfirmationService = TestBed.inject(
            FuseConfirmationService
        ) as jest.Mocked<FuseConfirmationService>;
    });

    function getMatErrorText() {
        const error = fixture.debugElement.query(By.css('mat-error'));
        return error ? error.nativeElement.textContent.trim() : null;
    }

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should allow user to select today or past date', () => {
        const today = new Date().toISOString().split('T')[0];
        component.externalTransfer.controls['transferDate'].setValue(today);
        fixture.detectChanges();
        expect(component.externalTransfer.controls['transferDate'].value).toBe(
            today
        );
    });

    it('should not allow user to select future date', () => {
        const futureDate = new Date();
        futureDate.setDate(futureDate.getDate() + 1);
        dateInput.value = futureDate.toISOString().split('T')[0];
        dateInput.dispatchEvent(new Event('input'));
        fixture.detectChanges();
        expect(component.externalTransfer.controls['transferDate'].value).toBe(
            ''
        );
    });

    it('should disable submit button', () => {
        const submitBtn = fixture.debugElement.query(
            By.css('#submitBtnId')
        ).nativeElement;
        expect(submitBtn.disabled).toBeTruthy();
    });

    it('should display required form validation error if date field is empty', () => {
        const dateFormControl = component.externalTransfer.get('transferDate');
        dateFormControl.setValue('');
        dateFormControl.markAsTouched();
        fixture.detectChanges();
        expect(getMatErrorText()).toBe('Transfer Date is required');
    });

    it('should display invalid date form validation error if invalid date entered', () => {
        const dateFormControl = component.externalTransfer.get('transferDate');
        dateFormControl.patchValue('12343434');
        dateFormControl.markAsTouched();
        fixture.detectChanges();
        expect(getMatErrorText()).toBe('Transfer Date is invalid');
    });

    it('should display max date form validation error if future date entered', () => {
        const futureDate = new Date();
        futureDate.setDate(futureDate.getDate() + 1);
        const maxDateFormControl =
            component.externalTransfer.get('transferDate');
        maxDateFormControl.patchValue(futureDate.toISOString());
        maxDateFormControl.markAsTouched();
        maxDateFormControl.updateValueAndValidity();
        fixture.detectChanges();
        expect(getMatErrorText()).toBe('Transfer Date cannot be in the future');
    });

    it('should add products', () => {
        jest.spyOn(service, 'verifyExternalTransferItem').mockReturnValue(
            of<
                MutationResult<{
                    addExternalTransferProduct: RuleResponseDTO<never>;
                }>
            >({
                data: {
                    addExternalTransferProduct: {
                        ruleCode: '200 OK',
                        notifications: [
                            {
                                notificationType: 'SUCCESS',
                                message: 'Product added successfully',
                            },
                        ] as NotificationDto[],
                        results: {
                            results: [
                                {
                                    externalTransferItems: [
                                        {
                                            id: 69,
                                            externalTransferId: 175,
                                            unitNumber: 'W036898786807',
                                            productCode: 'E0869V00',
                                            productFamily:
                                                'PLASMA_TRANSFUSABLE',
                                            createdByEmployeeId:
                                                '4c973896-5761-41fc-8217-07c5d13a004b',
                                        },
                                    ],
                                },
                            ],
                        },
                    },
                },
            } as MutationResult)
        );
        jest.spyOn(component as any, 'addedProductsComputed').mockReturnValue([
            {},
        ]);
        component.enterProduct(addedProductsMockData);
        expect(service.verifyExternalTransferItem).toHaveBeenCalled();
    });

    it('should display toaster with caution if the product has not been shipped', () => {
        jest.spyOn(service, 'verifyExternalTransferItem').mockReturnValue(
            of<
                MutationResult<{
                    addExternalTransferProduct: RuleResponseDTO<never>;
                }>
            >({
                data: {
                    addExternalTransferProduct: {
                        ruleCode: '400 BAD_REQUEST',
                        notifications: [
                            {
                                notificationType: 'CAUTION',
                                message: 'This product has not been shipped',
                            },
                        ] as NotificationDto[],
                    },
                },
            } as MutationResult)
        );
        jest.spyOn(toastr, 'show');
        component.enterProduct(addedProductsMockData);
        expect(toastr.show).toHaveBeenCalled();
        expect(toastr.show).toHaveBeenCalledWith(
            'This product has not been shipped',
            null,
            null,
            'warning'
        );
    });

    it('should display toaster with caution if the product is not in the same shipped location', () => {
        const ProductDetails: ExternalTransferItemDTO = {
            unitNumber: 'W036898786769',
            productCode: 'E9747D0E',
            employeeId: '',
            externalTransferId: 1,
            productFamily: '',
        };
        jest.spyOn(service, 'verifyExternalTransferItem').mockReturnValue(
            of<
                MutationResult<{
                    addExternalTransferProduct: RuleResponseDTO<never>;
                }>
            >({
                data: {
                    addExternalTransferProduct: {
                        ruleCode: '400 BAD_REQUEST',
                        notifications: [
                            {
                                notificationType: 'CAUTION',
                                message:
                                    'The product location doesnot match the last shipped location',
                            },
                        ] as NotificationDto[],
                    },
                },
            } as MutationResult)
        );
        jest.spyOn(toastr, 'show');
        component.enterProduct(ProductDetails);
        expect(toastr.show).toHaveBeenCalled();
        expect(toastr.show).toHaveBeenCalledWith(
            'The product location doesnot match the last shipped location',
            null,
            null,
            'warning'
        );
    });

    it('should display toaster with success message after external transfer process completed ', () => {
        jest.spyOn(service, 'completeExternalTransfer').mockReturnValue(
            of<
                MutationResult<{
                    completeExternalTransfer: RuleResponseDTO<never>;
                }>
            >({
                data: {
                    completeExternalTransfer: {
                        ruleCode: '200 OK',
                        notifications: [
                            {
                                notificationType: 'SUCCESS',
                                message:
                                    'External transfer completed successfully.',
                            },
                        ] as NotificationDto[],
                    },
                },
            } as MutationResult)
        );
        jest.spyOn(toastr, 'show');
        component.submitExternalTransfer();
        expect(toastr.show).toHaveBeenCalled();
    });

    it("should open cancel dialog when there's a confirmation notification", fakeAsync(async () => {
        const cancelExternalTransferSpy = jest
            .spyOn(service, 'cancelExternalTransferProcess')
            .mockReturnValue(
                of<
                    MutationResult<{
                        cancelExternalTransfer: RuleResponseDTO<never>;
                    }>
                >({
                    data: {
                        cancelExternalTransfer: {
                            ruleCode: '200 OK',
                            _links: null,
                            results: null,
                            notifications: [
                                {
                                    name: 'EXTERNAL_TRANSFER_CANCEL_CONFIRMATION',
                                    statusCode: 200,
                                    notificationType: 'CONFIRMATION',
                                    code: 200,
                                    action: null,
                                    reason: null,
                                    message:
                                        'When cancelling, all external transfer information will be removed. Are you sure you want to cancel?',
                                },
                            ],
                        },
                    },
                } as MutationResult)
            );
        const fuseConfirmationOpenDialogSpy = jest.spyOn(
            fuseConfirmationService,
            'open'
        );
        const toastrSpy = jest.spyOn(toastr, 'show');
        const pageCancelButton = fixture.debugElement.query(
            By.css('#cancelActionBtn')
        ).nativeElement as HTMLButtonElement;
        expect(pageCancelButton.disabled).toBeFalsy();
        pageCancelButton.click();
        component.cancelExternalTransfer();
        fixture.detectChanges();
        expect(cancelExternalTransferSpy).toHaveBeenCalled();
        expect(fuseConfirmationOpenDialogSpy).toHaveBeenCalled();
        expect(toastrSpy).not.toHaveBeenCalled();
    }));

    it('should get confirmation when cancel external transfer process is complete', () => {
        const cancelExternalTransferSpy = jest
            .spyOn(service, 'confirmCancelExternalTransferProcess')
            .mockReturnValue(
                of<
                    MutationResult<{
                        confirmCancelExternalTransfer: RuleResponseDTO<never>;
                    }>
                >({
                    data: {
                        confirmCancelExternalTransfer: {
                            ruleCode: '200 OK',
                            _links: {
                                next: '/external-transfer',
                            },
                            results: null,
                            notifications: [
                                {
                                    name: null,
                                    statusCode: 200,
                                    notificationType: 'SUCCESS',
                                    code: null,
                                    action: null,
                                    reason: null,
                                    message:
                                        'External transfer cancellation completed',
                                },
                            ],
                        },
                    },
                } as MutationResult)
            );

        const handleNavigationSpy = jest.spyOn(component, 'handleNavigation');
        const toastrSpy = jest.spyOn(toastr, 'show');
        component.confirmCancel();
        fixture.detectChanges();
        expect(cancelExternalTransferSpy).toHaveBeenCalled();
        expect(handleNavigationSpy).toHaveBeenCalledWith('/external-transfer');
        expect(toastrSpy).toHaveBeenCalled();
    });

    it('should redirect to the link', fakeAsync(async () => {
        const navigateBySpy = jest.spyOn(routerMock, 'navigateByUrl');
        const url = '/external-transfer';
        const newUrl = '/external-transfer/new';
        component.handleNavigation(url);
        expect(navigateBySpy).toHaveBeenCalledWith(newUrl);
    }));
});
