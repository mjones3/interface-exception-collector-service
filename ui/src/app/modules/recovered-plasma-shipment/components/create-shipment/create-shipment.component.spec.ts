import {
    ComponentFixture,
    fakeAsync,
    TestBed,
    tick,
} from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { MutationResult, QueryResult, ApolloError } from '@apollo/client';
import { provideMockStore } from '@ngrx/store/testing';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { UseCaseResponseDTO } from 'app/shared/models/use-case-response.dto';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { of, throwError } from 'rxjs';
import { RecoveredPlasmaShipmentResponseDTO } from '../../models/recovered-plasma.dto';
import { RecoveredPlasmaShipmentService } from '../../services/recovered-plasma-shipment.service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { CreateShipmentComponent } from './create-shipment.component';
import { AuthState } from 'app/core/state/auth/auth.reducer';
import { Validators } from '@angular/forms';
import { ModifyShipmentRequestDTO } from '../../graphql/mutation-definitions/modify-shipment.graphql';

describe('CreateShipmentComponent', () => {
    let component: CreateShipmentComponent;
    let fixture: ComponentFixture<CreateShipmentComponent>;
    let shipmentService: RecoveredPlasmaShipmentService;
    let recoveredPlasmaService: RecoveredPlasmaService;
    let toastr: ToastrService;
    let router: Router;
    let dialogRef: MatDialogRef<CreateShipmentComponent>;
    let mockMatDialog: jest.Mocked<MatDialog>;

    const initialState: AuthState = {
        id: 'mock-user-id',
        loaded: true,
    };

    const mockEmployeeId = 'emp123';

    mockMatDialog = {
        open: jest.fn(),
        closeAll: jest.fn(),
    } as Partial<MatDialog> as jest.Mocked<MatDialog>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                CreateShipmentComponent,
                ApolloTestingModule,
                NoopAnimationsModule,
                MatNativeDateModule,
                ToastrModule.forRoot(),
            ],
            providers: [
                provideMockStore({ initialState}),
                { provide: MAT_DIALOG_DATA, useValue: {} },
                {
                    provide: RecoveredPlasmaShipmentService,
                    useValue: {
                        createRecoveredPlasmaShipment: jest.fn(),
                        getProductTypeOptions: jest.fn(),
                    },
                },
                {
                    provide: MatDialogRef,
                    useValue: {
                        close: jest.fn(),
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(CreateShipmentComponent);
        component = fixture.componentInstance;
        shipmentService = TestBed.inject(RecoveredPlasmaShipmentService);
        dialogRef = TestBed.inject(MatDialogRef<CreateShipmentComponent>);
        router = TestBed.inject(Router);
        recoveredPlasmaService = TestBed.inject(RecoveredPlasmaService);
        toastr = TestBed.inject(ToastrService);
        jest.spyOn(shipmentService, 'getProductTypeOptions').mockReturnValue(
            of()
        );
        jest.spyOn(recoveredPlasmaService, 'findAllCustomers').mockReturnValue(
            of()
        );
        jest.spyOn(
            shipmentService,
            'createRecoveredPlasmaShipment'
        ).mockReturnValue(of());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should disable submit button', () => {
        const submitBtn = fixture.debugElement.query(
            By.css('#btnSubmit button')
        ).nativeElement;
        expect(submitBtn.disabled).toBeTruthy();
    });

    it('should hide carton tare weight', () => {
        component.showCartonTareWeight = false;
        fixture.detectChanges();
        expect(component).toBeTruthy();
        expect(
            fixture.debugElement.nativeElement.querySelector(
                '#cartonTareWeightId'
            )
        ).toBeFalsy();
    });

    it('should display mat error if date entered is less than min date', () => {
        const formControl = component.createShipmentForm.get('shipmentDate');
        const minDate = new Date(2024, 11, 26);
        formControl.setValue(minDate);
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        const error = fixture.debugElement.query(By.css('mat-error'));
        expect(error).toBeTruthy();
        expect(error.nativeElement.textContent).toContain(
            'Shipment Date cannot be in the past'
        );
    });

    it('should display mat error if customer name field is empty', () => {
        const formControl = component.createShipmentForm.get('customerName');
        formControl.enable();
        formControl.setValue('');
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        const error = fixture.debugElement.query(By.css('mat-error'));
        expect(error).toBeTruthy();
        expect(error.nativeElement.textContent).toContain(
            'Customer Name is required'
        );
    });

    it('should disable product type field if customer name field value is not selected', () => {
        const formControl = component.createShipmentForm.get('customerName');
        formControl.setValue('');
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        expect(
            component.createShipmentForm.controls.productType.disabled
        ).toBeTruthy();
    });

    it('should enable product type field if customer name field value is  selected', fakeAsync(() => {
        const formControl = component.createShipmentForm.get('customerName');
        formControl.enable();
        component.createShipmentForm.get('productType').disable();
        formControl.setValue('asa');
        tick(1000);
        fixture.detectChanges();
        expect(
            component.createShipmentForm.controls.productType.enabled
        ).toBeTruthy();
    }));

    it('should display mat error if product type field is empty', () => {
        const formControl = component.createShipmentForm.get('productType');
        formControl.enable();
        formControl.setValue('');
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        const error = fixture.debugElement.query(By.css('mat-error'));
        expect(error).toBeTruthy();
        expect(error.nativeElement.textContent).toContain(
            'Product Type is required'
        );
    });

    it('should enable submit button when form is valid', () => {
        const formControl = component.createShipmentForm;
        const shipmentDate = new Date(2030, 11, 11);
        formControl.get('customerName').setValue('sunrise');
        formControl.get('customerName').updateValueAndValidity();
        formControl.get('productType').enable();
        formControl.get('productType').setValue('plasma');
        formControl.get('cartonTareWeight').setValue(11);
        formControl.get('transportationReferenceNumber').setValue('');
        formControl.get('transportationReferenceNumber').setValue('');
        formControl.get('comments').setValue('Comments');
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        const submitBtn = fixture.debugElement.query(
            By.css('#btnSubmit button')
        );
        expect(formControl.valid).toBeTruthy();
        expect(submitBtn.nativeElement.disabled).not.toBeTruthy();
    });

    it('should load customers on init', () => {
        const mockCustomers = [
            { id: 1, name: 'Customer 1' },
            { id: 2, name: 'Customer 2' },
        ];
        const mockResponse = {
            data: { findAllCustomers: mockCustomers },
        } as QueryResult;
        jest.spyOn(recoveredPlasmaService, 'findAllCustomers').mockReturnValue(
            of(mockResponse)
        );

        component.ngOnInit();

        expect(recoveredPlasmaService.findAllCustomers).toHaveBeenCalled();
        expect(component.customerOptionList).toEqual(mockCustomers);
    });

    it('should enable productType and fetch options when customer name is entered', fakeAsync(() => {
        const mockProductTypes = [
            { productType: 'type1', productTypeDescription: 'Type 1' },
            { productType: 'type2', productTypeDescription: 'Type 2' },
        ];
        const mockResponse = {
            data: { findAllProductTypeByCustomer: mockProductTypes },
        } as QueryResult;
        jest.spyOn(shipmentService, 'getProductTypeOptions').mockReturnValue(
            of(mockResponse)
        );

        component.createShipmentForm
            .get('customerName')
            .setValue('Test Customer');
        tick(300); // Wait for debounceTime
        expect(
            component.createShipmentForm.get('productType').enabled
        ).toBeTruthy();
        expect(shipmentService.getProductTypeOptions).toHaveBeenCalledWith(
            'Test Customer'
        );
    }));

    it('should close dialog and not navigate if no success notification', () => {
        jest.spyOn(toastr, 'show');
        jest.spyOn(
            shipmentService,
            'createRecoveredPlasmaShipment'
        ).mockReturnValue(
            of<
                MutationResult<{
                    createShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
                }>
            >({
                data: {
                    createShipment: {
                        _links: {
                            next: null,
                        },
                        data: null,
                        notifications: [
                            {
                                type: 'WARN',
                                message: 'Warning message',
                            },
                        ],
                    },
                },
            } as MutationResult)
        );
        component.createShipmentForm.get('customerName').enable();
        component.createShipmentForm.get('productType').enable();
        const routerSpy = jest.spyOn(router, 'navigateByUrl');

        // Set valid form values
        component.createShipmentForm.patchValue({
            customerName: 'testCustomer',
            productType: 'testProduct',
            shipmentDate: '2029-12-31',
            transportaionReferenceNumber: 'REF123',
        });
        component.submit();
        expect(component.dialogRef.close).toHaveBeenCalled();
        expect(routerSpy).not.toHaveBeenCalled();
    });

    it('should handle successful shipment creation', () => {
        jest.spyOn(toastr, 'show');
        jest.spyOn(
            shipmentService,
            'createRecoveredPlasmaShipment'
        ).mockReturnValue(
            of<
                MutationResult<{
                    createShipment: UseCaseResponseDTO<RecoveredPlasmaShipmentResponseDTO>;
                }>
            >({
                data: {
                    createShipment: {
                        _links: {
                            next: '/recovered-plasma/:11/shipment-details',
                        },
                        data: {
                            cartonTareWeight: 11,
                            createDate: '2025-04-02T15:14:45.35109748Z',
                            createEmployeeId:
                                '4c973896-5761-41fc-8217-07c5d13a004b',
                            customerName: 'Bio Products',
                            id: 4,
                            locationCode: '123456789',
                            productType: 'RP_NONINJECTABLE_REFRIGERATED',
                            shipmentDate: '2025-04-23',
                            shipmentNumber: '27654',
                            status: 'OPEN',
                        },
                        notifications: [
                            {
                                type: 'SUCCESS',
                                message: 'Shipment created successfully',
                            },
                        ],
                    },
                },
            } as MutationResult)
        );

        jest.spyOn(dialogRef, 'close');
        component.createShipmentForm.get('productType').enable();

        // Set valid form values
        component.createShipmentForm.patchValue({
            customerName: 'testCustomer',
            productType: 'testProduct',
            cartonTareWeight: 10.5,
            shipmentDate: new Date('2029-12-31'),
            transportaionReferenceNumber: 'REF123',
        });
        component.submit();
        expect(component.dialogRef.close).toHaveBeenCalled();
        expect(
            shipmentService.createRecoveredPlasmaShipment
        ).toHaveBeenCalled();
    });

    it('should update form values when modifying existing shipment', () => {
        const mockShipmentData: RecoveredPlasmaShipmentResponseDTO = {
            id: 1,
            customerCode: 'TEST_CUSTOMER',
            shipmentDate: '2025-01-01',
            transportationReferenceNumber: 'REF123',
            status: 'OPEN',
            productType: 'TEST_PRODUCT',
            canModify: true
        };

        component.data = mockShipmentData;
        component.updateFormValues(mockShipmentData);

        expect(component.createShipmentForm.get('customerName').value).toBe('TEST_CUSTOMER');
        expect(component.createShipmentForm.get('shipmentDate').value).toBe('2025-01-01');
        expect(component.createShipmentForm.get('transportationReferenceNumber').value).toBe('REF123');
        expect(component.createShipmentForm.controls.comments.hasValidator(Validators.required)).toBeTruthy();
    });

    it('should disable product type when status is not OPEN', () => {
        const mockShipmentData: RecoveredPlasmaShipmentResponseDTO = {
            id: 1,
            customerCode: 'TEST_CUSTOMER',
            status: 'CLOSED',
            canModify: false
        };

        component.data = mockShipmentData;
        component.disableProductType();
        expect(component.createShipmentForm.get('productType').disabled).toBeTruthy();
    });

    it('should prepare modify shipment request correctly', () => {
        const mockShipmentData: RecoveredPlasmaShipmentResponseDTO = {
            id: 1,
            customerCode: 'TEST_CUSTOMER',
            productType: 'TEST_PRODUCT',
            status: 'OPEN',
            canModify: true
        };
        component.data = mockShipmentData;
        const mockRequest = {
            customerCode: 'TEST_CUSTOMER',
            productType: 'TEST_PRODUCT',
            shipmentDate: '2025-01-01',
            transportationReferenceNumber: 'REF123',
            modifyEmployeeId: mockEmployeeId,
            shipmentId: 1,
            comments: 'Test comment'
        }
        expect(mockRequest).toEqual({
            modifyEmployeeId: 'emp123',
            customerCode: 'TEST_CUSTOMER',
            productType: 'TEST_PRODUCT',
            shipmentDate:'2025-01-01',
            transportationReferenceNumber: 'REF123',
            shipmentId: 1,
            comments: 'Test comment'
        });
    });

    it('should handle error when loading customers fails', () => {
        const mockError = new ApolloError({ errorMessage: 'Test error' });
        jest.spyOn(recoveredPlasmaService, 'findAllCustomers').mockReturnValue(
            throwError(() => mockError)
        );
        jest.spyOn(toastr, 'error');

        component.ngOnInit();

        expect(toastr.error).toHaveBeenCalledWith('Something Went Wrong.');
        expect(component.customerOptionList).toEqual([]);
    });
});
