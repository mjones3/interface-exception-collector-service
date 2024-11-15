import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
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
export class DateRangePickerComponent {
    @Input() title!: string;
    @Input() formGroup!: FormGroup;
    @Input() dateFromFormControlName!: string;
    @Input() dateToFormControlName!: string;
    @Input() dateRangeId = 'dateRangeControl';
    @Input() maxDate?: Date; // Add this line

    today = new Date();
}
