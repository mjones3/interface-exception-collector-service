import { CommonModule, NgTemplateOutlet } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { customerOptionDto } from 'app/modules/external-transfer/models/external-transfer.dto';

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
    @Input() placeholder: string;
    @Input() formGroup!: FormGroup;
    @Input() matSelectId: string;
    @Input() control!: FormControl;
    @Output() selectionChange: EventEmitter<void> = new EventEmitter<void>();
    @Input() items: customerOptionDto[];
    @Input() disabled = false;
    @Input() required = false;

    get placeholderText() {
        return `Filter ${this.title}`;
    }

    get fieldErrorMessage() {
        return `${this.title} is required`;
    }

    get itemList(): customerOptionDto[] {
        return this.items ?? [];
    }

    isNotAMatch(name: string, valueToFilter = ''): boolean {
        return !name
            .toLowerCase()
            .includes(valueToFilter?.toLowerCase().trim());
    }

    onSelectionChange() {
        this.selectionChange.emit();
    }
}
