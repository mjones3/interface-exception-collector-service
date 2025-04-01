import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrModule } from 'ngx-toastr';
import { CreateShipmentComponent } from './create-shipment.component';
import { RecoveredPlasmaShipmentService } from '../../services/recovered-plasma-shipment.service';
import { RecoveredPlasmaService } from '../../services/recovered-plasma.service';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { of } from 'rxjs';
import { ToastrImplService } from '@shared';
import { QueryResult } from '@apollo/client';
import { Router } from '@angular/router';

describe('CreateShipmentComponent', () => {
    let component: CreateShipmentComponent;
    let fixture: ComponentFixture<CreateShipmentComponent>;
    let shipmentService: RecoveredPlasmaShipmentService;
    let recoveredPlasmaService: RecoveredPlasmaService;

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
                    provide: MatDialogRef,
                    useValue: {
                        close: jest.fn(),
                    },
                }
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(CreateShipmentComponent);
        component = fixture.componentInstance;
        shipmentService = TestBed.inject(RecoveredPlasmaShipmentService);
        recoveredPlasmaService = TestBed.inject(RecoveredPlasmaService);
        jest.spyOn(shipmentService, 'getProductTypeOptions').mockReturnValue(of());
        jest.spyOn(recoveredPlasmaService, 'findAllCustomers').mockReturnValue(of());
        jest.spyOn(shipmentService, 'createRecoveredPlasmaShipment').mockReturnValue(of());
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
        const formControl = component.createShipmentForm.get(
            'scheduledShipmentDate'
        );
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
        const formControl = component.createShipmentForm.get('cartonTareWeight');
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
        const scheduledShipmentDate = new Date(2030, 11, 11);
        formControl.get('customerName').setValue('sunrise');
        formControl.get('customerName').updateValueAndValidity();
        formControl.get('productType').enable();
        formControl.get('productType').setValue('plasma');
        formControl.get('cartonTareWeight').setValue(11);
        formControl.get('transportationReferenceNumber').setValue('');
        formControl
            .get('scheduledShipmentDate')
            .setValue(scheduledShipmentDate.toISOString());
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
            { id: 2, name: 'Customer 2' }
        ];
        const mockResponse = { data: { findAllCustomers: mockCustomers } } as QueryResult;
        jest.spyOn(recoveredPlasmaService, 'findAllCustomers').mockReturnValue(of(mockResponse));

        component.ngOnInit();

        expect(recoveredPlasmaService.findAllCustomers).toHaveBeenCalled();
        expect(component.customerOptionList).toEqual(mockCustomers);
    });

    it('should load product types when customer is selected', () => {
        const mockProductTypes = [
            { productType: 'type1', productTypeDescription: 'Type 1' },
            { productType: 'type2', productTypeDescription: 'Type 2' }
        ];
        const mockResponse = { data: { findAllProductTypeByCustomer: mockProductTypes } } as QueryResult;
        jest.spyOn(shipmentService, 'getProductTypeOptions').mockReturnValue(of(mockResponse));

        component.createShipmentForm.get('customerName').setValue('testCustomer');

        expect(shipmentService.getProductTypeOptions).toHaveBeenCalledWith('testCustomer');
        expect(component.productTypeOptions).toEqual([
            { code: 'type1', name: 'Type 1' },
            { code: 'type2', name: 'Type 2' }
        ]);
    });
});
