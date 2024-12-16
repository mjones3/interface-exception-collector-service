import { CommonModule } from '@angular/common';
import { AfterContentInit, Component, Input, OnDestroy } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Subject, distinctUntilChanged, takeUntil } from 'rxjs';

@Component({
    selector: 'app-date-range-picker',
    standalone: true,
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
    @Input() matDatepickerMaxMessage: string;
    @Input() invalidDateMessage = 'Date is Invalid';
    @Input() noDateInformedMessage = 'Date is Required';

    private readonly destroyed$ = new Subject<void>();

    get startControl() {
        return this.formGroup.get('start');
    }
    get endControl() {
        return this.formGroup.get('end');
    }

    ngAfterContentInit(): void {
        this.startControl.valueChanges
            .pipe(distinctUntilChanged(), takeUntil(this.destroyed$))
            .subscribe(() =>
                setTimeout(() => this.endControl.updateValueAndValidity())
            );

        this.endControl.valueChanges
            .pipe(distinctUntilChanged(), takeUntil(this.destroyed$))
            .subscribe(() =>
                setTimeout(() => this.startControl.updateValueAndValidity())
            );
    }

    ngOnDestroy(): void {
        this.destroyed$.next();
    }

    errorMessage(): string | null {
        const startControl = this.formGroup.get('start');
        const endControl = this.formGroup.get('end');

        if (startControl?.errors) {
            const errorKeys = Object.keys(startControl.errors);
            if (errorKeys.includes('matDatepickerParse')) {
                return this.invalidDateMessage;
            } else if (errorKeys.includes('invalidDate')) {
                return this.invalidDateMessage;
            } else if (errorKeys.includes('required')) {
                return this.noDateInformedMessage;
            } else if (errorKeys.includes('matDatepickerMin')) {
                return this.matDatepickerMinErrorMessage;
            }
        } else if (endControl?.errors) {
            const errorKeys = Object.keys(endControl.errors);
            if (errorKeys.includes('matEndDateInvalid')) {
                return this.matEndDateInvalidMessage;
            } else if (errorKeys.includes('matDatepickerMax')) {
                return this.matDatepickerMaxMessage;
            } else if (errorKeys.includes('matDatepickerParse')) {
                return this.invalidDateMessage;
            } else if (errorKeys.includes('invalidDate')) {
                return this.invalidDateMessage;
            } else if (errorKeys.includes('required')) {
                return this.noDateInformedMessage;
            }
        }

        return null;
    }
}
