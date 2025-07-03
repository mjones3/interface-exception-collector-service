import { SelectionModel } from '@angular/cdk/collections';
import { NgClass, NgTemplateOutlet } from '@angular/common';
import {
    ChangeDetectionStrategy,
    Component,
    EventEmitter,
    Input,
    OnChanges,
    OnInit,
    Output,
    SimpleChanges,
    TemplateRef,
    ViewEncapsulation,
    booleanAttribute,
    forwardRef,
    numberAttribute
} from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { FuseCardComponent } from '@fuse/components/card';
import { OptionPicker, OptionStatus } from 'app/shared/models';
import { Orientation } from 'app/shared/types';
import { BaseControlValueAccessor } from '../../forms/base-control-value-accessor';

@Component({
    selector: 'biopro-options-picker',
    exportAs: 'bioproOptionsPicker',
    templateUrl: './options-picker.component.html',
    styleUrls: ['./options-picker.component.scss'],
    changeDetection: ChangeDetectionStrategy.OnPush,
    encapsulation: ViewEncapsulation.None,
    standalone: true,
    imports: [
        NgClass,
        NgTemplateOutlet,
        MatButtonModule,
        MatCardModule,
        MatIconModule,
        FuseCardComponent
    ],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => OptionsPickerComponent),
            multi: true
        }
    ]
})
export class OptionsPickerComponent<TData extends OptionPicker = OptionPicker>
    extends BaseControlValueAccessor<string | string[]>
    implements OnInit, OnChanges {
    @Input({ required: true }) options: TData[] = [];
    @Input({ required: true }) optionsLabel: string;
    @Input() optionsSelected: TData[];
    @Input({ transform: booleanAttribute }) multiple = false;
    @Input({ transform: numberAttribute }) maxRows = 4;
    @Input({ transform: numberAttribute }) maxColumnsAllowed = 3;
    @Input() orientation: Orientation = Orientation.COLUMN;
    @Input() buttonTemplate: TemplateRef<{ option: TData }>;
    @Input({ transform: booleanAttribute }) fullSize = false;

    @Output() optionChange = new EventEmitter<TData | TData[]>();

    selectedOptions: SelectionModel<TData>;
    columns: number;

    ngOnInit(): void {
        this.selectedOptions = new SelectionModel<TData>(
            this.multiple,
            this.optionsSelected
        );
        if (this.options?.length) {
            this.calculateColumns();
        }
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes.optionsSelected?.currentValue) {
            this.selectedOptions = new SelectionModel<TData>(
                this.multiple,
                changes.optionsSelected.currentValue
            );
        }

        if (
            (changes.options &&
                (changes.options.currentValue !==
                    changes.options.previousValue ||
                    changes.options.currentValue)) ||
            changes.optionsSelected?.currentValue
        ) {
            this.calculateColumns();
        }
    }

    calculateColumns(): void {
        if (this.orientation === Orientation.COLUMN) {
            const columnsCalculated = Math.ceil(
                this.options.length / this.maxRows
            );
            this.columns =
                columnsCalculated > this.maxColumnsAllowed
                    ? this.maxColumnsAllowed
                    : columnsCalculated;
        } else {
            this.columns =
                this.options.length > this.maxColumnsAllowed
                    ? this.maxColumnsAllowed
                    : this.options.length;
        }
    }

    optionClick(value: TData): void {
        if (this.selectedOptions.isSelected(value)) {
            this.selectedOptions.deselect(value);
        } else {
            this.selectedOptions.toggle(value);
        }
        const values = this.multiple
            ? this.selectedOptions.selected
            : this.selectedOptions.selected[0];
        // Emit an option change value
        this.optionChange.emit(values);
    }

    getOptionColor(option: TData) {
        return this.selectedOptions.isSelected(option) ? 'primary' : 'default';
    }

    trackByFn(index: number, item: TData | OptionStatus) {
        return item['id'] || index;
    }
}
