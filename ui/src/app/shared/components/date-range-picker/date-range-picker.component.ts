import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, Input } from '@angular/core';
import {
    AbstractControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    ValidationErrors,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';

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
export class DateRangePickerComponent implements AfterViewInit {
    @Input() title!: string;
    @Input() formGroup!: FormGroup;
    @Input() dateFromFormControlName!: string;
    @Input() dateToFormControlName!: string;
    @Input() dateRangeId = 'dateRangeControl';
    @Input() maxDate?: Date;
    @Input() disabled = false;

    today = new Date();

    ngAfterViewInit(): void {
        this.formGroup.addValidators([
            this.dateRangeRequiredValidator.bind(this),
        ]);
    }

    dateRangeRequiredValidator(
        control: AbstractControl
    ): ValidationErrors | null {
        const startDateCtrl = control.get(this.dateFromFormControlName);
        const endDateCtrl = control.get(this.dateToFormControlName);

        if (startDateCtrl && endDateCtrl) {
            const startDate = startDateCtrl.value;
            const endDate = endDateCtrl.value;

            if (
                startDateCtrl.touched ||
                startDateCtrl.dirty ||
                endDateCtrl.touched ||
                endDateCtrl.dirty
            ) {
                // Check if both dates are provided
                if (!startDate && endDate) {
                    return { dateRangeRequired: true };
                }
                if (startDate && !endDate) {
                    return { dateRangeRequired: true };
                }

                // Ensure end date is after start date
                if (startDate && endDate) {
                    const startDateObj = new Date(startDate);
                    const endDateObj = new Date(endDate);

                    if (endDateObj < startDateObj) {
                        return { matEndDateInvalid: true };
                    }

                    // Calculate the difference in years
                    const timeDifference =
                        endDateObj.getTime() - startDateObj.getTime();
                    const dayDifference = timeDifference / (1000 * 3600 * 24);
                    const yearDifference = dayDifference / 365;
                    console.log('yearDifference', yearDifference);

                    if (yearDifference > 2) {
                        return {
                            dateRangeExceedsTwoYears:
                                this.dateToFormControlName,
                        };
                    }
                }
            }
        }

        return null;
    }
}
