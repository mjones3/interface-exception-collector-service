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
        component.dateFromFormControlName = 'dateFrom';
        component.dateToFormControlName = 'dateTo';
        component.formGroup = new FormGroup({
            dateFrom: new FormControl(),
            dateTo: new FormControl(),
        });
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
