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
    @Input() matSelectId = 'selectControlInput';
    @Input() control!: FormControl;
    @Input() items: SelectOptionDto[] = [];

    get placeholderText() {
        return `Filter ${this.title}`;
    }

    firstSelectedValue(key: string, source: SelectOptionDto[]): string {
        return '';
        if (
            !this.formGroup.controls[key].value ||
            this.formGroup.controls[key].value?.length === 0
        ) {
            return '';
        }
        return this.formGroup.controls[key].value[0] === 'select-all'
            ? 'All'
            : source
                  .filter((item) =>
                      this.formGroup.controls[key].value.includes(
                          item.optionKey
                      )
                  )
                  .map((item) => item.optionDescription)
                  .toString();
    }

    get itemList(): SelectOptionDto[] {
        return this.items ? this.items : [];
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
