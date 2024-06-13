import { Component, EventEmitter, Input, OnInit, Output, TemplateRef } from '@angular/core';
import { FormControl } from '@angular/forms';
import { TranslateService } from '@ngx-translate/core';
import { getTextWidth, Option } from '@rsa/commons';
import Keyboard from 'simple-keyboard';
import { InputType } from '../../shared/models/input-type.enum';
import { KeyboardTypeEnum } from '../../shared/models/keyboard-type.enum';

@Component({
  selector: 'rsa-on-screen-keyboard',
  templateUrl: './on-screen-keyboard.component.html',
  styleUrls: ['./on-screen-keyboard.component.scss'],
})
export class OnScreenKeyboardComponent implements OnInit {
  @Input() keyboardType: KeyboardTypeEnum = KeyboardTypeEnum.TEXT;
  @Input() closeOnReturn: boolean;
  @Input() value = '';
  @Input() placeholder = '';
  @Input() inputType: InputType = InputType.TEXT;
  @Input() inputTemplate: TemplateRef<any>;
  @Input() regex = '';
  @Input() validators;
  @Input() customErrors = {};
  @Input() options: Option[];
  @Input() optionsLabel = 'name';
  @Input() maxLength: number;
  @Input() autocomplete: string;
  @Input() isDecimal: boolean;

  @Output() optionSelected: EventEmitter<Option> = new EventEmitter();
  @Output() returnPressed: EventEmitter<string> = new EventEmitter();

  readonly maxWidth = 750;
  readonly defaultTextFont =
    '14px Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji"';
  keyboard: Keyboard;
  shiftPressed: boolean;
  filteredOptions: Option[] = [];
  actualPage = 0;
  widths: number[];
  pageWithAmount: number[] = [];
  inputTypeEnum = InputType;
  inputFocus = false;
  inputControl = new FormControl('');
  upperCase: boolean;

  constructor(private translateService: TranslateService) {}

  ngOnInit(): void {
    this.shiftPressed = false;
    this.keyboard = this.initKeyboard(this.keyboardType);
    this.closeOnReturn = this.inputType === InputType.TEXT || this.inputType === InputType.NUMBER;
    if (this.validators) {
      this.inputControl.setValidators(this.validators);
    }

    // Set value from app input to keyboard input
    if (this.value !== '' && this.value !== null) {
      this.value = this.keyboardType === KeyboardTypeEnum.NUMERIC ? this.value.toString() : this.value;
      this.keyboard.setInput(this.value, 'default');
      this.inputControl.setValue(this.value);
      this.filterOptions();
    }
    setTimeout(() => (this.inputFocus = true), 250);

    this.inputControl.valueChanges.subscribe(newValue => {
      this.value = newValue;
      this.keyboard.setInput(newValue);
      if (this.filteredOptions) {
        this.filterOptions();
      }
    });
  }

  // Init Keyboard based on keyboardType
  initKeyboard(keyboardType: KeyboardTypeEnum): Keyboard {
    switch (keyboardType) {
      case KeyboardTypeEnum.NUMERIC:
        return this.getNumericKeyboard();

      case KeyboardTypeEnum.TEXT:
        return this.getDefaultKeyboard();

      default:
        return this.getDefaultKeyboard();
    }
  }

  // Returns Configurations for Numeric Keyboard
  getNumericKeyboard(): Keyboard {
    return new Keyboard({
      onChange: input => this.onChange(input),
      onKeyPress: button => this.onKeyPress(button),
      theme: 'hg-theme-default hg-numeric',
      layout: {
        default: this.isDecimal
          ? ['1 2 3', '4 5 6', '7 8 9', '0 .', '{bksp} {enter}']
          : ['1 2 3', '4 5 6', '7 8 9', '0', '{bksp} {enter}'],
      },
      display: {
        '{enter}': 'Enter',
        '{bksp}': 'Backspace',
      },
      preventMouseDownDefault: true,
    });
  }

  // Returns Configurations for Default Keyboard
  getDefaultKeyboard(): Keyboard {
    return new Keyboard({
      onChange: input => this.onChange(input),
      onKeyPress: button => this.onKeyPress(button),
      theme: 'hg-theme-default hg-theme-ios',
      layout: {
        default: [
          'q w e r t y u i o p {bksp}',
          'a s d f g h j k l {enter}',
          '{shift} z x c v b n m , . {shift}',
          '{alt} {space} {altright}',
        ],
        shift: [
          'Q W E R T Y U I O P {bksp}',
          'A S D F G H J K L {enter}',
          '{shiftactivated} Z X C V B N M , . {shiftactivated}',
          '{alt} {space} {altright}',
        ],
        alt: [
          '1 2 3 4 5 6 7 8 9 0 {bksp}',
          `@ # $ & * ( ) ' " {enter}`,
          '{shift} % - + = / ; : ! ? {shift}',
          '{default} {space} {back}',
        ],
      },
      display: {
        '{alt}': '.?123',
        '{shift}': '⇧',
        '{shiftactivated}': '⇧',
        '{enter}': 'Enter',
        '{bksp}': '⌫',
        '{altright}': '.?123',
        '{space}': ' ',
        '{default}': 'ABC',
        '{back}': '⇦',
      },
    });
  }

  onChange = (input: string) => {
    this.syncValue(input);
    this.filterOptions();
    // Synchronizing input caret position
    const caretPosition = this.keyboard.caretPosition;
    if (caretPosition !== null) {
      this.setInputCaretPosition(this.value, caretPosition);
    }
  }

  onKeyPress = (button: string) => {
    // Emit event for Enter key
    if (button === '{enter}') {
      if (this.closeOnReturn) {
        this.triggerInputValidationIfEmpty();
        this.validateAndEmitValue();
      } else {
        this.syncValue(`${this.value}\n`);
        setTimeout(() => {
          this.inputFocus = true;
        });
      }
    }
    // Handle toggles
    if (button.includes('{') && button.includes('}')) {
      this.handleLayoutChange(button);
    }
  }

  syncValue(value: any) {
    this.value = value;
    this.inputControl.setValue(value);
    this.keyboard.setInput(value);
  }

  onInputChange = (event: any) => {
    this.syncValue(event.target.value);
    this.filterOptions();
  }

  handleLayoutChange(button: string): void {
    const currentLayout = this.keyboard.options.layoutName;
    let layoutName = '';

    switch (button) {
      case '{shift}':
      case '{shiftactivated}':
      case '{default}':
        layoutName = currentLayout === 'default' ? 'shift' : 'default';
        break;

      case '{alt}':
      case '{altright}':
        layoutName = currentLayout === 'alt' ? 'default' : 'alt';
        break;

      default:
        break;
    }

    if (layoutName) {
      this.keyboard.setOptions({
        layoutName: layoutName,
      });
    }
  }

  setInputCaretPosition = (elem: any, pos: number) => {
    if (elem.setSelectionRange) {
      elem.focus();
      elem.setSelectionRange(pos, pos);
    }
  }

  private triggerInputValidationIfEmpty() {
    if (!this.value) {
      this.inputControl.setValue(this.value);
    }
  }

  validateAndEmitValue(optionSelected?: Option): void {
    this.triggerInputValidationIfEmpty();
    if (!this.inputControl.valid) {
      return;
    }
    if (optionSelected) {
      this.optionSelected.emit(optionSelected);
    } else {
      if (this.isDecimal) {
        this.value = (this.value.includes('.') ? +this.value : +this.value / 100).toString();
      }
      this.returnPressed.emit(this.value);
    }
  }

  selectOption(option: Option) {
    this.syncValue(option[this.optionsLabel]);
    this.actualPage = 0;
    this.validateAndEmitValue(option);
  }

  swipeLeft() {
    this.nextOptionPage();
  }

  swipeRight() {
    this.prevOptionPage();
  }

  getStartPage() {
    return this.pageWithAmount.slice(0, this.actualPage).reduce((acc, value) => acc + value, 0);
  }

  getEndPage() {
    return this.getStartPage() + this.pageWithAmount[this.actualPage];
  }

  private setWidths() {
    this.widths = this.filteredOptions.map(
      option => getTextWidth(option[this.optionsLabel], this.defaultTextFont) + 32
    );
    this.pageWithAmount = [];
    let width = 0;
    let totalItem = 0;
    this.widths.forEach(w => {
      if (w + width <= this.maxWidth - 32) {
        width += w;
        totalItem++;
      } else {
        this.pageWithAmount.push(totalItem);
        width = w;
        totalItem = 1;
      }
    });
    if (totalItem > 0) {
      this.pageWithAmount.push(totalItem);
    }
  }

  private filterOptions() {
    if (this.options) {
      this.filteredOptions = this.options.filter(value => {
        const opt = value[this.optionsLabel];
        return this.translateService
          .instant(opt)
          .toLowerCase()
          .includes(this.value ? this.value.toLowerCase() : '');
      });
      this.setWidths();
    }
  }

  private nextOptionPage() {
    if (this.actualPage + 1 < this.pageWithAmount.length) {
      this.actualPage++;
    }
    this.filterOptions();
  }

  private prevOptionPage() {
    if (this.actualPage > 0) {
      this.actualPage--;
    }
    this.filterOptions();
  }

  onPaste() {
    // Synchronizing keyboard caret position with value length
    this.keyboard.caretPosition = this.value.length;
  }
}
