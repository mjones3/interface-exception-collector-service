import { CommonModule, NgTemplateOutlet } from '@angular/common';
import { Component, Input } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { SelectOptionDto } from 'app/shared/models';

@Component({
    selector: 'biopro-search-select',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatInputModule,
        CommonModule,
        MatIconModule,
        MatSelectModule,
        MatButtonModule,
        NgTemplateOutlet,
    ],
    templateUrl: './search-select.component.html',
})
export class SearchSelectComponent {
    @Input() title!: string;
    @Input() controlName!: string;
    @Input() formGroup!: FormGroup;
    @Input() matSelectId: string;
    @Input() control!: FormControl;
    @Input() items: SelectOptionDto[];
    @Input() disabled = false;
    @Input() required = false;

    get placeholderText() {
        return `Filter ${this.title}`;
    }

    get fieldErrorMessage() {
        return `${this.title} Required`;
    }

    get itemList(): SelectOptionDto[] {
        return this.items ?? [];
    }

    isNotAMatch(
        optionDescription: string,
        valueToFilter = ''
    ): boolean {
        return !optionDescription
            .toLowerCase()
            .includes(valueToFilter?.toLowerCase().trim());
    }
}
