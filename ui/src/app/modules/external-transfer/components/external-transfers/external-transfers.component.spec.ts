import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import {
    MAT_DATE_FORMATS,
    MAT_NATIVE_DATE_FORMATS,
    MatNativeDateModule,
    NativeDateModule,
} from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { By } from '@angular/platform-browser';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { ExternalTransfersComponent } from './external-transfers.component';

describe('ExternalTransfersComponent', () => {
    let component: ExternalTransfersComponent;
    let fixture: ComponentFixture<ExternalTransfersComponent>;
    let dateInput: HTMLInputElement;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                ExternalTransfersComponent,
                ReactiveFormsModule,
                MatInputModule,
                MatDatepickerModule,
                MatNativeDateModule,
                MatFormFieldModule,
                NativeDateModule,
                NoopAnimationsModule,
            ],
            providers: [
                {
                    provide: MAT_DATE_FORMATS,
                    useValue: MAT_NATIVE_DATE_FORMATS,
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ExternalTransfersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        dateInput = fixture.debugElement.query(By.css('input')).nativeElement;
    });

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
        expect(
            component.externalTransfer.controls['transferDate'].value
        ).toBeNull();
    });
});
