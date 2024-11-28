import { CommonModule } from '@angular/common';
import { AfterContentInit, Component, Input, OnDestroy } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import {
    DateAdapter,
    MAT_DATE_FORMATS,
    provideNativeDateAdapter,
} from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Subject, distinctUntilChanged, takeUntil } from 'rxjs';
import { TwoDigitsDateAdapter } from '../../adapter/two-digits-date.adapter';

export const CUSTOM_DATE_FORMATS = {
    parse: { dateInput: 'MM/DD/YYYY' },
    display: {
        dateInput: 'MM/DD/YYYY',
        monthYearLabel: 'MMM YYYY',
        dateA11yLabel: 'MM/DD/YYYY',
        monthYearA11yLabel: 'MMMM YYYY',
    },
};

@Component({
    selector: 'app-date-range-picker',
    standalone: true,
    providers: [provideNativeDateAdapter()],
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        FormsModule,
        CommonModule,
        MatDividerModule,
        MatIconModule,
        MatSelectModule,
        MatButtonModule,
        MatDatepickerModule,
    ],
    templateUrl: './date-range-picker.component.html',
    viewProviders: [
        { provide: DateAdapter, useClass: TwoDigitsDateAdapter },
        { provide: MAT_DATE_FORMATS, useValue: CUSTOM_DATE_FORMATS },
    ],
})
export class DateRangePickerComponent implements AfterContentInit, OnDestroy {
    @Input() title!: string;
    @Input() formGroup!: FormGroup;
    @Input() dateRangeId = 'dateRangeControl';
    @Input() maxDate?: Date;
    @Input() minDate?: Date;
    @Input() disabled = false;
    @Input() matDatepickerMinErrorMessage: string;
    @Input() matEndDateInvalidMessage: string;
    @Input() matDatepickerMaxMessage: string;

    private readonly destroyed$ = new Subject<void>();

    ngAfterContentInit(): void {
        const startFormControl = this.formGroup.get('start');
        const endFormControl = this.formGroup.get('end');

        startFormControl.valueChanges
            .pipe(distinctUntilChanged(), takeUntil(this.destroyed$))
            .subscribe(() =>
                setTimeout(() => endFormControl.updateValueAndValidity())
            );

        endFormControl.valueChanges
            .pipe(distinctUntilChanged(), takeUntil(this.destroyed$))
            .subscribe(() =>
                setTimeout(() => startFormControl.updateValueAndValidity())
            );
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
    }
}
