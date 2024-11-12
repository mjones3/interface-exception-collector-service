import { CommonModule, NgTemplateOutlet } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
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
export class DateRangePickerComponent {
    @Input() title!: string;
    @Input() formGroup!: FormGroup;
    @Input() dateFromFormControlName!: string;
    @Input() dateToFormControlName!: string;
}
