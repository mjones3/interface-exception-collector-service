import { CommonModule } from '@angular/common';
import { AfterContentInit, Component, Input, OnDestroy } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Subject, distinctUntilChanged, takeUntil } from 'rxjs';

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
