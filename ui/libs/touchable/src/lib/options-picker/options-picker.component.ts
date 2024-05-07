import { SelectionModel } from '@angular/cdk/collections';
import {
  ChangeDetectionStrategy,
  Component,
  EventEmitter,
  forwardRef,
  Input,
  OnChanges,
  OnInit,
  Output,
  TemplateRef,
  ViewEncapsulation,
} from '@angular/core';
import { NG_VALUE_ACCESSOR } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { BaseControlValueAccessor, Option, Orientation } from '@rsa/commons';
import { isArray } from 'lodash';

@Component({
  selector: 'rsa-options-picker',
  exportAs: 'rsaOptionsPicker',
  templateUrl: './options-picker.component.html',
  styleUrls: ['./options-picker.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
  encapsulation: ViewEncapsulation.None,
  providers: [{ provide: NG_VALUE_ACCESSOR, useExisting: forwardRef(() => OptionsPickerComponent), multi: true }],
})
export class OptionsPickerComponent extends BaseControlValueAccessor<string | string[]> implements OnInit, OnChanges {
  @Input() options: any;
  @Input() optionsSelected = [];
  @Input() optionsLabel: string | string[];
  @Input() optionsId = 'optionButtons';
  @Input() buttonPropertyId: string;
  @Input() headerTitle: string;
  @Input() showTitle = true;
  @Input() transparentHeader: boolean;
  @Input() multiple = false;
  @Input() embedded: boolean;
  @Input() maxRows = 4;
  @Input() orientation: Orientation = Orientation.COLUMN;
  @Input() fullWidth: boolean;
  @Input() showStatus = false;
  @Input() textContent: string;
  @Input() defaultIconName: string;
  @Input() p4 = false;
  @Input() buttonTemplate: TemplateRef<any>;

  @Output() optionChange = new EventEmitter<Option | Option[]>();

  selectedOptions: SelectionModel<any>;
  columns: number;
  readonly maxColumnsAllowed = 3;

  constructor(private translateService: TranslateService) {
    super();
  }

  ngOnInit(): void {
    this.selectedOptions = new SelectionModel<any>(this.multiple, this.optionsSelected);
    if (this.options && this.options.length > 0) {
      this.calculateColumns();
    }
  }

  ngOnChanges(changes): void {
    if (changes.optionsSelected && changes.optionsSelected.currentValue) {
      this.selectedOptions = new SelectionModel<any>(this.multiple, changes.optionsSelected.currentValue);
    }
    if (
      (changes.options && changes.options.currentValue) ||
      (changes.optionsSelected && changes.optionsSelected.currentValue)
    ) {
      this.calculateColumns();
    }
  }

  getButtonId(option: object) {
    if (this.buttonPropertyId) {
      if (option[this.buttonPropertyId]) {
        return option[this.buttonPropertyId];
      }
    }

    return this.getOptionLabel(option);
  }

  getOptionLabel(option: object): string {
    if (typeof this.optionsLabel === 'object') {
      let buttonLabel = '';
      this.optionsLabel.forEach(label => {
        if (option[label]) {
          buttonLabel =
            buttonLabel === ''
              ? this.translateService.instant(option[label])
              : `${buttonLabel} ${this.translateService.instant(option[label])}`;
        }
      });
      return buttonLabel;
    } else {
      return option[this.optionsLabel];
    }
  }

  calculateColumns(): void {
    if (this.orientation === Orientation.COLUMN) {
      const columnsCalculated = Math.ceil(this.options.length / this.maxRows);
      this.columns = columnsCalculated > this.maxColumnsAllowed ? this.maxColumnsAllowed : columnsCalculated;
    } else {
      this.columns = this.options.length > this.maxColumnsAllowed ? this.maxColumnsAllowed : this.options.length;
    }
  }

  writeValue(value: string | string[]): void {
    if (value !== '') {
      super.writeValue(value);
      this.selectedOptions.select(isArray(value) ? value : [value]);
      this.selectedOptions.toggle(value);
    }
  }

  selectOption(selectedValue: any): void {
    if (isArray(selectedValue)) {
      selectedValue.forEach(value => this.selectedOptions.toggle(value));
    } else {
      this.selectedOptions.toggle(selectedValue);
    }
    const values = this.multiple ? this.selectedOptions.selected : this.selectedOptions.selected[0];
    // Emit an option change value
    this.optionChange.emit(values);
    // Emit onChange CVA
    this.onChange(this.getCVAValue());
  }

  deselectOption(selectedValue: any | any[]): void {
    if (isArray(selectedValue)) {
      selectedValue.forEach(value => this.selectedOptions.deselect(value));
    } else {
      this.selectedOptions.deselect(selectedValue);
    }
  }

  getOptionColor(option: any) {
    return this.selectedOptions.isSelected(option) ? 'primary' : 'default';
  }

  private getCVAValue(): SelectionModel<any> {
    return this.multiple ? this.selectedOptions.selected : this.selectedOptions.selected[0];
  }
}
