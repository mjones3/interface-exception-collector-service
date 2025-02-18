import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { FuseCardComponent } from '@fuse/components/card/public-api';

@Component({
    selector: 'app-filters',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        FormsModule,
        CommonModule,
        MatIconModule,
        FuseCardComponent,
        MatButtonModule,
        MatBadgeModule,
    ],
    templateUrl: './filters.component.html',
})
export class FiltersComponent {
    @Input() disabled: boolean;
    @Input() resetDisabled: boolean;
    @Input() appliedTotalFilterCount = 0;
    @Output() search: EventEmitter<void> = new EventEmitter<void>();
    @Output() resetSearchFilter: EventEmitter<void> = new EventEmitter<void>();
    @Input() showFilters = false;
    @Output() showFilterControl: EventEmitter<boolean> =
        new EventEmitter<boolean>();

    toggleFilters() {
        this.showFilters = !this.showFilters;
        this.showFilterControl.emit(this.showFilters);
    }

    applyFilter() {
        this.search.emit();
        this.toggleFilters();
    }

    resetFilters() {
        this.disabled = true;
        this.resetSearchFilter.emit();
    }
}
