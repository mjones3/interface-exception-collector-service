import { AfterViewInit, Directive, inject, Input, OnDestroy } from '@angular/core';
import { MatSelect } from '@angular/material/select';
import { MatOption } from '@angular/material/core';
import { Subscription } from 'rxjs';

@Directive({
    selector: 'mat-option[selectAll]',
    standalone: true,
})
export class SelectAllDirective implements AfterViewInit, OnDestroy {
    @Input({ required: true }) allValues: string[] = [];

    private _matSelect = inject(MatSelect);
    private _matOption = inject(MatOption);

    private _subscriptions: Subscription[] = [];

    ngAfterViewInit(): void {
        const parentSelect = this._matSelect;
        const parentFormControl = parentSelect.ngControl.control;

        // For changing other option selection based on select all
        this._subscriptions.push(
            this._matOption.onSelectionChange.subscribe((ev) => {
                if (ev.isUserInput) {
                    if (ev.source.selected) {
                        parentFormControl?.setValue(this.allValues);
                        this._matOption.select(false);
                    } else {
                        parentFormControl?.setValue([]);
                        this._matOption.deselect(false);
                    }
                }
            }),
            parentSelect.optionSelectionChanges.subscribe((v) => {
                if (v.isUserInput && v.source.value !== this._matOption.value) {
                    if (!v.source.selected) {
                        this._matOption.deselect(false);
                        v.source.deselect(false);
                        parentFormControl.setValue(parentSelect.value.filter(item => item !== 'select-all' && item !== v.source.value));
                    } else {
                        const selectedValues = parentFormControl?.value?.filter(item => this.allValues.includes(item));
                        if ((selectedValues?.length + 1) === this.allValues.length) {
                            parentFormControl.setValue(['select-all',...parentSelect.value]);
                            this._matOption.select(false);
                        }
                    }
                }
            }),
        );
    }

    ngOnDestroy(): void {
        this._subscriptions.forEach((s) => s.unsubscribe());
    }
}


