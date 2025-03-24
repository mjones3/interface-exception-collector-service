import { CommonModule } from '@angular/common';
import { Component, input, OnInit, output } from '@angular/core';
import {
    FormBuilder,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
    Validators,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { DateRangePickerComponent } from 'app/shared/components/date-range-picker/date-range-picker.component';
import { FiltersComponent } from 'app/shared/components/filters/filters.component';
import { MultipleSelectComponent } from 'app/shared/components/multiple-select/multiple-select.component';

@Component({
    selector: 'biopro-filter-shipment',
    standalone: true,
    imports: [
        FiltersComponent,
        ReactiveFormsModule,
        MatInputModule,
        FormsModule,
        CommonModule,
        MatDividerModule,
        MatIconModule,
        MatSelectModule,
        MatButtonModule,
        MultipleSelectComponent,
        MatDatepickerModule,
        DateRangePickerComponent,
    ],
    templateUrl: './filter-shipment.component.html',
})
export class FilterShipmentComponent implements OnInit {
    showFilters = input(false);
    toggleFilters = output<boolean>();
    totalFieldsApplied = 0;
    today = new Date();

    searchShipmentForm: FormGroup;

    constructor(private formBuilder: FormBuilder) {}

    ngOnInit(): void {
        this.initForm();
    }

    private initForm() {
        this.searchShipmentForm = this.formBuilder.group({
            shipmentNumber: ['', [Validators.maxLength(50)]],
            shipmentStatus: [''],
            productFamily: [''],
            customer: [''],
        });
    }

    resetFilters(): void {
        this.searchShipmentForm.reset();
    }

    toggleFilter(toggleFlag: boolean): void {
        this.toggleFilters.emit(toggleFlag);
    }
}
