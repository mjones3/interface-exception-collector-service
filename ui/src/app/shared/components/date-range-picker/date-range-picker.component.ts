import { CommonModule, NgTemplateOutlet } from '@angular/common';
import { Component, Input, OnInit } from '@angular/core';
import {
    AbstractControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    ValidationErrors,
    ValidatorFn,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { SelectAllDirective } from '../../directive/select-all/select-all.directive';
import { FiltersComponent } from '../filters/filters.component';

@Component({
    selector: 'app-date-range-picker',
    standalone: true,
    providers: [
        provideNativeDateAdapter(),
        // { provide: MAT_DATE_LOCALE, useValue: 'en-US' },
        // {provide: MAT_DATE_FORMATS, useValue: {
        //         parse: {
        //             dateInput: 'MM/DD/YYYY',
        //         },
        //         display: {
        //             dateInput: 'MM/dd/yyyy',
        //             monthYearLabel: 'MMM YYYY',
        //             dateA11yLabel: 'LL',
        //             monthYearA11yLabel: 'MMMM YYYY',
        //         },
        //     }}
    ],
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        FormsModule,
        CommonModule,
        MatDividerModule,
        MatIconModule,
        FiltersComponent,
        MatSelectModule,
        MatButtonModule,
        NgTemplateOutlet,
        SelectAllDirective,
        MatDatepickerModule,
    ],
    templateUrl: './date-range-picker.component.html',
})
export class DateRangePickerComponent implements OnInit {
    @Input() title!: string;
    @Input() formGroup!: FormGroup;
    @Input() dateFromFormControlName!: string;
    @Input() dateToFormControlName!: string;

    ngOnInit(): void {
        const dateFromControl = this.formGroup.get(
            this.dateFromFormControlName
        );
        const dateToControl = this.formGroup.get(this.dateToFormControlName);

        if (dateFromControl && dateToControl) {
            dateFromControl.setValidators([
                Validators.required,
                this.dateRangeValidator(),
            ]);
            dateToControl.setValidators([
                Validators.required,
                this.dateRangeValidator(),
            ]);

            // Update validity after setting validators to ensure the form reacts immediately
            dateFromControl.updateValueAndValidity();
            dateToControl.updateValueAndValidity();
        }
    }

    private dateRangeValidator(): ValidatorFn {
        return (_: AbstractControl): ValidationErrors | null => {
            const startDateControl = this.formGroup.get(
                this.dateFromFormControlName
            );
            const endDateControl = this.formGroup.get(
                this.dateToFormControlName
            );

            if (!startDateControl || !endDateControl) return null;

            const start = startDateControl.value;
            const end = endDateControl.value;

            // Check if the start or end dates are invalid
            if (start && !this.isDateValid(start)) {
                return { invalidDate: true };
            }
            if (end && !this.isDateValid(end)) {
                return { invalidDate: true };
            }

            // Check if the start date is after the end date
            if (start && end && start > end) {
                return { invalidDateRange: true };
            }

            return null;
        };
    }

    private isDateValid(date: any): boolean {
        return !isNaN(new Date(date).getTime());
    }
}
