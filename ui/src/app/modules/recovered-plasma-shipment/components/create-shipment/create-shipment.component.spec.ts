import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatNativeDateModule } from '@angular/material/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { CreateShipmentComponent } from './create-shipment.component';

describe('CreateShipmentComponent', () => {
    let component: CreateShipmentComponent;
    let fixture: ComponentFixture<CreateShipmentComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                CreateShipmentComponent,
                NoopAnimationsModule,
                MatNativeDateModule,
            ],
            providers: [
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
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should disable submit button', () => {
        const submitBtn = fixture.debugElement.query(
            By.css('#actionBtnSubmit')
        ).nativeElement;
        expect(submitBtn.disabled).toBeTruthy();
    });
});
