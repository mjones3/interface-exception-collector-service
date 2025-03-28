import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrModule, ToastrService } from 'ngx-toastr';
import { CreateShipmentComponent } from './create-shipment.component';

describe('CreateShipmentComponent', () => {
    let component: CreateShipmentComponent;
    let fixture: ComponentFixture<CreateShipmentComponent>;
    let toastr: ToastrService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                CreateShipmentComponent,
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
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(CreateShipmentComponent);
        component = fixture.componentInstance;
        toastr = TestBed.inject(ToastrService);
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
        const formControl = component.createShipment.get(
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
        const formControl = component.createShipment.get('cartonTareWeight');
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
        const formControl = component.createShipment.get('customerName');
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
        const formControl = component.createShipment.get('customerName');
        formControl.setValue('');
        formControl.markAsTouched();
        formControl.updateValueAndValidity();
        fixture.detectChanges();
        expect(
            component.createShipment.controls.productType.disabled
        ).toBeTruthy();
    });

    it('should display mat error if product type field is empty', () => {
        const formControl = component.createShipment.get('productType');
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
        const formControl = component.createShipment;
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
});
