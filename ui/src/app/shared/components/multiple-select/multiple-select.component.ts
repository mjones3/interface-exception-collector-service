import { CommonModule, NgTemplateOutlet } from '@angular/common';
import { Component, Input } from '@angular/core';
import {
    FormControl,
    FormGroup,
    FormsModule,
    ReactiveFormsModule,
} from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { SelectOptionDto } from '@shared';
import {
    firstSelectedOption,
    selectedOptionsCount,
} from 'app/shared/utils/mat-select-trigger.utils';
import { SelectAllDirective } from '../../directive/select-all/select-all.directive';
import { FiltersComponent } from '../filters/filters.component';

@Component({
    selector: 'app-multiple-select',
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
    ],
    templateUrl: './multiple-select.component.html',
    styleUrl: './multiple-select.component.scss',
})
export class MultipleSelectComponent {
    @Input() title!: string;
    @Input() controlName!: string;
    @Input() formGroup!: FormGroup;
    @Input() matSelectId = 'multipleSelectControl';
    @Input() control!: FormControl;
    @Input() items: SelectOptionDto[] = [];
    @Input() disabled = false;

    get placeholderText() {
        return `Filter ${this.title}`;
    }

    firstSelectedOption(items: SelectOptionDto[]) {
        const selectedOptions =
            (this.formGroup.get(this.controlName)?.value as string[]) || [];
        return firstSelectedOption(selectedOptions, items);
    }

    selectedOptionsCount() {
        return selectedOptionsCount(
            (this.formGroup.get(this.controlName)?.value as string[]) || []
        );
    }

    get itemList(): SelectOptionDto[] {
        return this.items ? this.items : [];
    }

    isNotAMatch(optionDescription: string, valueToFilter: string) {
        return !optionDescription
            .toLowerCase()
            .includes(valueToFilter?.toLowerCase());
    }

    selectOptionKeys(selectOptions: SelectOptionDto[]): string[] {
        return selectOptions?.length > 0
            ? selectOptions.map((option) => option.optionKey)
            : [];
    }

    search(source: SelectOptionDto[], filterValue: string): SelectOptionDto[] {
        if (!filterValue || filterValue === '') {
            return source;
        }
        return source.filter((optionValue) =>
            optionValue.optionDescription
                .toLowerCase()
                .includes(filterValue.toLowerCase())
        );
    }
}
