import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { FormGroup } from '@angular/forms';
import { MatOption } from '@angular/material/core';
import { MatSelect } from '@angular/material/select';
import { ValidationType } from '../../pipes/validation.pipe';
import { Option } from '../../shared/models/option.model';
import { SelectOption } from './select-options';

const ALL_LABEL = 'select-all.label';

@Component({
  selector: 'rsa-select-list',
  templateUrl: 'select-list.component.html',
  styleUrls: ['select-list.component.scss'],
})
export class SelectListComponent implements OnInit, OnChanges {
  readonly validationType = ValidationType;
  @Input() set doResetField(val: { [value: string]: boolean }) {
    if (val && val.value && this.matSelect && this.matSelect['control']) {
      this.matSelect['control'].markAsUntouched();
      this.matSelect['control'].markAsPristine();
    }
  }
  constructor() {}

  get selectedCount() {
    return (
      this.selectedOptions && ((this.selectedOptions as SelectOption[]) || []).filter(p => p !== this.allOption).length
    );
  }

  get selectedOption_(): SelectOption {
    return Array.isArray(this.selectedOptions) ? this.selectedOptions[0] : (this.selectedOptions as SelectOption);
  }

  get isSelectedOptionList(): boolean {
    return (this.selectedOptions as SelectOption[]).length !== undefined;
  }

  get selectedText(): string[] {
    return (this.selectedOptions as SelectOption[]).map(p => p.description_key);
  }
  @Input() options: Array<SelectOption>;

  @Input() selectedOptions: SelectOption[] | SelectOption;
  @Input() showSelectAll;
  @Input() label;
  @Input() style;
  @Input() multiple: boolean;
  @Input() showSelectedCount: boolean;
  @Input() showSearch: boolean;
  @Input() required: boolean;
  @Input() showError: boolean;
  @Input() form: FormGroup;
  @Output() selectionChange: EventEmitter<SelectOption[] | SelectOption> = new EventEmitter<
    SelectOption[] | SelectOption
  >();
  @ViewChild('selectAllOption') selectAllOption: MatOption;
  @ViewChild('selectList') matSelect: MatSelect;
  searchRegionValue: string;
  filteredOptions: SelectOption[] = [];
  selectedValue: string;

  allOption: Option = {
    selectionKey: ALL_LABEL,
    descriptionKey: ALL_LABEL,
    id: ALL_LABEL,
  };

  ngOnChanges(changes: SimpleChanges): void {
    if ('options' in changes && changes.options.currentValue) {
      this.filteredOptions = changes.options.currentValue.map(r => {
        r['visible'] = true;
        return r;
      });
    }
  }

  checkAllOptions(selected: boolean): void {
    if (selected) {
      const formattedCollection = this.filteredOptions
        .filter(p => p['visible'] && (this.selectedOptions as SelectOption[]).indexOf(p) === -1)
        .concat(this.selectedOptions);
      this.selectionChange.emit(formattedCollection);
    } else {
      this.selectionChange.emit(
        (this.selectedOptions as SelectOption[]).filter(
          i => !this.filteredOptions.find(f => f.value === i.value && f.visible === true)
        )
      );
    }
  }

  ngOnInit(): void {
    this.filteredOptions = this.options.map(r => {
      r['visible'] = true;
      return r;
    });
  }

  onChange(searchText) {
    const visibleArray = [];
    this.filteredOptions.forEach(element => {
      const isVisible = element.description.toLowerCase().includes((searchText ? searchText : '').toLowerCase());
      element['visible'] = isVisible;
      if (isVisible) {
        visibleArray.push(element);
      }
    });

    if (searchText && (this.selectedOptions as SelectOption[]).length) {
      if (visibleArray.every(option => (this.selectedOptions as SelectOption[]).some(t => t.value === option.value))) {
        this.selectAllOption.select();
      } else {
        this.selectAllOption.deselect();
      }
    }
  }

  checkOneOption(): void {
    if (this.multiple && this.showSelectAll) {
      if (this.selectAllOption.selected) {
        this.selectAllOption.deselect();
      } else {
        if (
          this.filteredOptions
            ?.filter(filtered => filtered.visible === true)
            .every(option => (this.selectedOptions as SelectOption[]).some(t => t.value === option.value))
        ) {
          this.selectAllOption.select();
        }
      }
    }
  }

  /* Emits the selected value to the parent component */
  onSelectionChange($event) {
    this.selectedValue = $event.value;
    this.selectionChange.emit($event.value);
  }

  compareCategoryObjects(object1: SelectOption, object2: SelectOption) {
    return object1 && object2 && object1.value === object2.value;
  }
}
