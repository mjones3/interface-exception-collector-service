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
import { provideMockStore } from '@ngrx/store/testing';
import { ToastrImplService } from '@shared';
import { ApolloTestingModule } from 'apollo-angular/testing';
import { ToastrModule } from 'ngx-toastr';
import { of } from 'rxjs';
import { ExternalTransferService } from '../../services/external-transfer.service';
import { ExternalTransfersComponent } from './external-transfers.component';

describe('ExternalTransfersComponent', () => {
    let component: ExternalTransfersComponent;
    let fixture: ComponentFixture<ExternalTransfersComponent>;
    let dateInput: HTMLInputElement;
    let service: ExternalTransferService;
    let toastr: ToastrImplService;

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
        jest.spyOn(service, 'customerInfo').mockReturnValue(of());
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
        const error = fixture.debugElement.query(By.css('mat-error'));
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
});
