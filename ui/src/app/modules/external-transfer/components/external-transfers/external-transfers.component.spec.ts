import { ComponentFixture, TestBed } from '@angular/core/testing';
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
import { MutationResult } from '@apollo/client';
import { provideMockStore } from '@ngrx/store/testing';
import { NotificationDto, ToastrImplService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ExternalTransferItemDTO } from '../../models/external-transfer.dto';
import { ExternalTransferService } from '../../services/external-transfer.service';
import { ExternalTransfersComponent } from './external-transfers.component';

describe('ExternalTransfersComponent', () => {
    let component: ExternalTransfersComponent;
    let fixture: ComponentFixture<ExternalTransfersComponent>;
    let dateInput: HTMLInputElement;
    let service: ExternalTransferService;
    let toastr: ToastrImplService;

    const addedProductsMockData = {
        unitNumber: 'W036898786807',
        productCode: 'E0869V00',
        employeeId: '4c973896-5761-41fc-8217-07c5d13a004b',
        productFamily: 'PLASMA_TRANSFUSABLE',
        externalTransferId: 1,
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
                provideMockStore({}),
                {
                    provide: MAT_DATE_FORMATS,
                    useValue: MAT_NATIVE_DATE_FORMATS,
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ExternalTransfersComponent);
        component = fixture.componentInstance;
        service = TestBed.inject(ExternalTransferService);
        toastr = TestBed.inject(ToastrImplService);
        jest.spyOn(service, 'customerInfo').mockReturnValue(of());
        jest.spyOn(service, 'verifyExternalTransferItem').mockReturnValue(of());
        jest.spyOn(service, 'completeExternalTransfer').mockReturnValue(of());
        toastr = TestBed.inject(ToastrImplService);
        fixture.detectChanges();
        dateInput = fixture.debugElement.query(By.css('input')).nativeElement;
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
});
