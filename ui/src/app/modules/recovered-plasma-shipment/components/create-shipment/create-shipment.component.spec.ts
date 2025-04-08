import {
    ComponentFixture,
    fakeAsync,
    TestBed,
    tick,
} from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { Router } from '@angular/router';
import { MutationResult, QueryResult } from '@apollo/client';
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrImplService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { RuleResponseDTO } from 'app/shared/models/rule.model';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { RecoveredPlasmaShipmentService } from '../../services/recovered-plasma-shipment.service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { CreateShipmentComponent } from './create-shipment.component';

describe('CreateShipmentComponent', () => {
    let component: CreateShipmentComponent;
    let fixture: ComponentFixture<CreateShipmentComponent>;
    let shipmentService: RecoveredPlasmaShipmentService;
    let recoveredPlasmaService: RecoveredPlasmaService;
    let toastr: ToastrImplService;
    let router: Router;
    let dialogRef: MatDialogRef<CreateShipmentComponent>;

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
                provideMockStore({}),
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
        toastr = TestBed.inject(ToastrImplService);
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

    it('should validate carton tare weight 3 decimal places', () => {
        const formControl =
            component.createShipmentForm.get('cartonTareWeight');
        formControl.setValue(11.3333);
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        const error = fixture.debugElement.query(By.css('mat-error'));
        expect(error).toBeTruthy();
        expect(error.nativeElement.textContent).toContain(
            'Carton Tare Weight is invalid'
        );
    });

    it('should not allow comma in carton tare weight', () => {
        const formControl =
            component.createShipmentForm.get('cartonTareWeight');
        formControl.setValue('1,333');
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        const error = fixture.debugElement.query(By.css('mat-error'));
        expect(error).toBeTruthy();
        expect(error.nativeElement.textContent).toContain(
            'Carton Tare Weight is invalid'
        );
    });

    it('should not allow alphabetic characters in carton tare weight', () => {
        const formControl =
            component.createShipmentForm.get('cartonTareWeight');
        formControl.setValue('asasasas');
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        const error = fixture.debugElement.query(By.css('mat-error'));
        expect(error).toBeTruthy();
        expect(error.nativeElement.textContent).toContain(
            'Carton Tare Weight is invalid'
        );
    });

    it('should display mat error if customer name field is empty', () => {
        const formControl = component.createShipmentForm.get('customerName');
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
        formControl.get('shipmentDate').setValue(shipmentDate.toISOString());
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
                    createShipment: RuleResponseDTO<never>;
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

        component.createShipmentForm.get('productType').enable();
        const routerSpy = jest.spyOn(router, 'navigateByUrl');

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
                    createShipment: RuleResponseDTO<never>;
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
});
