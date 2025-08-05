import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FormControl, FormGroup } from '@angular/forms';
import {
    MAT_DATE_FORMATS,
    MAT_NATIVE_DATE_FORMATS,
    NativeDateModule,
} from '@angular/material/core';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';
import { DateRangePickerComponent } from './date-range-picker.component';

describe('DateRangePickerComponent', () => {
    let component: DateRangePickerComponent;
    let fixture: ComponentFixture<DateRangePickerComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                DateRangePickerComponent,
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

        fixture = TestBed.createComponent(DateRangePickerComponent);
        component = fixture.componentInstance;

        component.formGroup = new FormGroup({
            start: new FormControl(),
            end: new FormControl(),
        });
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('errorMessage()', () => {
        it('should return null when no dates are selected', () => {
            expect(component.errorMessage()).toBeNull();
        });

        it('should return error message when end date is before start date', () => {
            component.formGroup.get('start').setValue('2024-02-15');
            component.formGroup.get('end').setValue('2024-02-10');

            expect(component.errorMessage()).toBe('Date range is invalid');
        });

        it('should return null when dates are valid', () => {
            component.formGroup.get('start').setValue('2024-02-10');
            component.formGroup.get('end').setValue('2024-02-15');

            expect(component.errorMessage()).toBeNull();
        });

        it('should return null when only start date is selected', () => {
            component.formGroup.get('start').setValue('2024-02-10');
            component.formGroup.get('end').setValue(null);

            expect(component.errorMessage()).toBeNull();
        });

        it('should return null when only end date is selected', () => {
            component.formGroup.get('start').setValue(null);
            component.formGroup.get('end').setValue('2024-02-15');

            expect(component.errorMessage()).toBeNull();
        });

        it('should handle invalid date formats', () => {
            component.formGroup.get('start').setValue('invalid-date');
            component.formGroup.get('end').setValue('2024-02-15');

            expect(component.errorMessage()).toBe('Date is invalid');
        });
    });
});
